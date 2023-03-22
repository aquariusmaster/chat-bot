package com.anderb.chatbot;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.anderb.chatbot.Config.*;

@Slf4j
public class BotApplication implements RequestStreamHandler {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final AbsSender SENDER = new ChatBot(BOT_USERNAME, BOT_TOKEN, BOT_URL);

    @Override
    public void handleRequest(InputStream input, OutputStream output, Context context) {
        Update update = getUpdate(input);
        if (!isValidUpdate(update)) {
            log.debug("Invalid update request");
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
            log.debug("Update: {}", inputJson);
            return MAPPER.readValue(inputJson, Update.class);
        } catch (Exception e) {
            log.debug("Exception while parsing: {}", e.getMessage());
            throw new RuntimeException("Failed to parse update!", e);
        }
    }

    @SneakyThrows
    private void sendResponse(Long chatId, String text) {
        try {
            sendMessage(chatId, text.replaceAll("([_\\[*])", "\\\\$1"), ParseMode.MARKDOWN);
        } catch (TelegramApiException e) {
            log.debug("Cannot send message in markdown: {}", e.getMessage());
            sendMessage(chatId, text, null);
        }
    }

    private void sendMessage(Long chatId, String text, String parseMode) throws TelegramApiException {
        for (String message : divideString(text, 4095)) {
            SendMessage sendMessage = SendMessage.builder()
                    .chatId(chatId)
                    .text(message)
                    .parseMode(parseMode)
                    .build();
            SENDER.execute(sendMessage);
        }
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
        String allowedUsers = ALLOWED_USERS;
        if (allowedUsers == null || allowedUsers.isBlank()) {
            return false;
        }
        if ("*".equals(allowedUsers)) {
            return true;
        }
        return allowedUsers.contains(userId.toString());
    }

    public static List<String> divideString(String input, int maxLength) {
        List<String> substrings = new ArrayList<>();
        int inputLength = input.length();
        for (int i = 0; i < inputLength; i += maxLength) {
            int endIndex = Math.min(i + maxLength, inputLength);
            substrings.add(input.substring(i, endIndex));
        }
        return substrings;
    }

}
