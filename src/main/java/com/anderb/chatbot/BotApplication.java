package com.anderb.chatbot;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
public class BotApplication implements RequestHandler<Update, Void> {

    private static final int MESSAGE_SIZE_LIMIT = 4095;

    private final AbsSender bot = initBot();
    private final DynamoDbChatHistoryClient chatHistoryClient = new DynamoDbChatHistoryClient();
    private final ObjectMapper objectMapper = getObjectMapper();

    private final ChatGptService chatGptService = new ChatGptService(objectMapper, chatHistoryClient);

    @Override
    public Void handleRequest(Update update, Context context) {
        handleRequest(update);
        return null;
    }

    public void handleRequest(Update update) {
        log.debug("Update: {}", update);
        if (!isValidUpdate(update)) {
            log.debug("Invalid update request");
            return;
        }
        Long chatId = update.getMessage().getChatId();
        String prompt = update.getMessage().getText();
        if (prompt.equals("/clear")) {
            chatHistoryClient.putChatSession(chatId, Collections.emptyList());
            sendResponse(chatId, "Message history was cleaned");
            return;
        }
        String response = chatGptService.callChat(chatId, prompt);
        sendResponse(chatId, response);
    }

    @SneakyThrows
    private void sendResponse(Long chatId, String text) {
        try {
            sendMessage(chatId, text.replaceAll("([_\\[*])", "\\\\$1"), ParseMode.MARKDOWN);
        } catch (Exception e) {
            log.debug("Cannot send message in markdown: {}", e.getMessage());
            sendMessage(chatId, text, null);
        }
    }

    private void sendMessage(Long chatId, String text, String parseMode) throws TelegramApiException {
        for (String message : divideString(text, MESSAGE_SIZE_LIMIT)) {
            SendMessage sendMessage = SendMessage.builder()
                    .chatId(chatId)
                    .text(message)
                    .parseMode(parseMode)
                    .build();
            bot.execute(sendMessage);
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
        String allowedUsers = Config.ALLOWED_USERS;
        if (allowedUsers == null || allowedUsers.isBlank()) {
            return false;
        }
        if ("*".equals(allowedUsers)) {
            return true;
        }
        return allowedUsers.contains(userId.toString());
    }

    public List<String> divideString(String input, int maxLength) {
        List<String> substrings = new ArrayList<>();
        int inputLength = input.length();
        for (int i = 0; i < inputLength; i += maxLength) {
            int endIndex = Math.min(i + maxLength, inputLength);
            substrings.add(input.substring(i, endIndex));
        }
        return substrings;
    }

    private AbsSender initBot() {
        return new TelegramWebhookBot(Config.BOT_TOKEN) {

            @Override
            public String getBotUsername() {
                return Config.BOT_USERNAME;
            }

            @Override
            public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
                return null;
            }

            @Override
            public String getBotPath() {
                return Config.BOT_URL;
            }
        };
    }

    private ObjectMapper getObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        return mapper;
    }

}
