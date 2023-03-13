package com.anderb.chatbot;

import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

public class ChatBot extends TelegramWebhookBot {

    private final String botPath;
    private final String botName;

    public ChatBot(String botName, String botToken, String botPath) {
        super(botToken);
        this.botName = botName;
        this.botPath = botPath;
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        return null;
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public String getBotPath() {
        return botPath;
    }
}
