package com.anderb.chatbot;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static java.lang.System.getenv;

public class BotApplication implements RequestStreamHandler {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final AbsSender SENDER = new ChatBot(getenv("bot_username"), getenv("bot_token"), getenv("bot_url"));
    private final String[] ESCAPE_CHARS = {"[", "_", "*"};

    @Override
    public void handleRequest(InputStream input, OutputStream output, Context context) {
        Update update = getUpdate(input);
        if (!isValidUpdate(update)) {
            Logger.debug("Invalid update request");
            return;
        }
        String prompt = update.getMessage().getText();
        String response = ChatGptService.callChat(prompt);
        Long chatId = update.getMessage().getChatId();
        sendResponse(chatId, response);
    }

    private Update getUpdate(InputStream input) {
        try {
            String inputJson = IOUtils.toString(input, StandardCharsets.UTF_8);
            Logger.debug("Update: %s", inputJson);
            return MAPPER.readValue(inputJson, Update.class);
        } catch (Exception e) {
            Logger.error("Exception while parsing: %s", e.getMessage());
            throw new RuntimeException("Failed to parse update!", e);
        }
    }

    @SneakyThrows
    private void sendResponse(Long chatId, String text) {
        boolean success = sendMarkDown(chatId, text);
        if (!success) {
            Logger.error("Trying to fallback with simplified text");
            sendMessage(chatId, text, null);
        }
    }

    private boolean sendMarkDown(Long chatId, String text) {
        int retry = 0;
        do {
            try {
                sendMessage(chatId, text, ParseMode.MARKDOWN);
                return true;
            } catch (TelegramApiException e) {
                Logger.error( "Retry: %d Exception while sending response: %s", retry + 1, e.getMessage());
                if (!e.getMessage().contains("Bad Request: can't parse entities")) {
                    return false;
                }
                String escapeChar = ESCAPE_CHARS[retry++];
                text = text.replace(escapeChar, "\\" + escapeChar);
            }
        } while (retry < ESCAPE_CHARS.length);
        return false;
    }

    private void sendMessage(Long chatId, String text, String parseMode) throws TelegramApiException {
        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .parseMode(parseMode)
                .build();
        SENDER.execute(sendMessage);
    }

    private boolean isValidUpdate(Update update) {
        return Optional.ofNullable(update)
                .map(Update::getMessage)
                .filter(message -> message.getChatId() != null)
                .filter(message -> StringUtils.isNotBlank(message.getText()))
                .map(Message::getFrom)
                .map(User::getId)
                .filter(this::isAllowedUser)
                .isPresent();
    }

    private boolean isAllowedUser(Long userId) {
        String allowedUsers = getenv("allowed_users");
        if (allowedUsers == null || allowedUsers.isBlank()) {
            return false;
        }
        if ("*".equals(allowedUsers)) {
            return true;
        }
        return allowedUsers.contains(userId.toString());
    }

}
