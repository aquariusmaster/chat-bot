package com.anderb.chatbot;

import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

public class ChatBot extends TelegramWebhookBot {

    private final String botPath;
    private final String botName;

    public ChatBot(String botPath, String botName, String botToken) {
        super(botToken);
        this.botPath = botPath;
        this.botName = botName;
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        Message message = update.getMessage();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(message.getChatId()));
        if (message.hasText()) {
            sendMessage.setText("Hello world");
            return sendMessage;
        }
        return null;

    }

    @Override
    public String getBotPath() {
        return botPath;
    }

    @Override
    public String getBotUsername() {
        return botName;
    }
}
