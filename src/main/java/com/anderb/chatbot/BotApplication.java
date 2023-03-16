package com.anderb.chatbot;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;

import static java.lang.System.getenv;

public class BotApplication implements RequestStreamHandler {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final AbsSender SENDER = new ChatBot(getenv("bot_username"), getenv("bot_token"), getenv("bot_url"));

    @Override
    public void handleRequest(InputStream input, OutputStream output, Context context) {
        Update update = getUpdate(input);
        if (!isValidUpdate(update)) return;
        String prompt = update.getMessage().getText();
        String response = ChatGptService.chatCall(prompt);
        Long chatId = update.getMessage().getChatId();
        sendResponse(chatId, response);
    }

    private Update getUpdate(InputStream input) {
        try {
            return MAPPER.readValue(input, Update.class);
        } catch (Exception e) {
            System.err.println("Exception while parsing: " + e.getMessage());
            throw new RuntimeException("Failed to parse update!", e);
        }
    }

    private void sendResponse(Long chatId, String text) {
        try {
            SendMessage sendMessage = SendMessage.builder()
                    .chatId(chatId)
                    .text(text)
                    .parseMode(ParseMode.MARKDOWN)
                    .build();
            SENDER.execute(sendMessage);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send message!", e);
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
