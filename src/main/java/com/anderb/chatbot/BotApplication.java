package com.anderb.chatbot;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.lang.System.getenv;

public class BotApplication implements RequestStreamHandler {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final AbsSender SENDER = new ChatBot(getenv("bot_username"), getenv("bot_token"), getenv("bot_url"));
    private static final Set<Long> WHITELISTED_USERS = getWhitelistedUsers();

    @Override
    public void handleRequest(InputStream input, OutputStream output, Context context) {
        var update = getUpdate(input);
        if (!isValidUser(update)) return;
        handleUpdate(update);
    }

    private Update getUpdate(InputStream input) {
        try {
            return MAPPER.readValue(input, Update.class);
        } catch (Exception e) {
            System.err.println("Exception while parsing: " + e.getMessage());
            throw new RuntimeException("Failed to parse update!", e);
        }
    }

    private void handleUpdate(Update update) {
        String prompt = update.getMessage().getText();
        String response = ChatGptService.chatCall(prompt);
        SendMessage sendMessage = SendMessage.builder()
                .chatId(update.getMessage().getChatId())
                .text(response)
                .parseMode(ParseMode.MARKDOWN)
                .build();
        try {
            SENDER.execute(sendMessage);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send message!", e);
        }
    }

    private static Set<Long> getWhitelistedUsers() {
        return Optional.ofNullable(getenv("whitelist_users"))
                .filter(Predicate.not(String::isBlank))
                .map(users -> users.split(","))
                .stream()
                .flatMap(Arrays::stream)
                .map(String::trim)
                .filter(Predicate.not(String::isBlank))
                .map(Long::parseLong)
                .collect(Collectors.toSet());
    }

    private boolean isValidUser(Update update) {
        return Optional.ofNullable(update)
                .map(Update::getMessage)
                .map(Message::getFrom)
                .map(User::getId)
                .filter(WHITELISTED_USERS::contains)
                .isPresent();
    }

}
