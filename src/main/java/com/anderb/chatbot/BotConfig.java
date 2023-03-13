package com.anderb.chatbot;

public class BotConfig {

    public String getBotName() {
        return System.getenv("bot_username");
    }

    public String getWebhookPath() {
        return System.getenv("bot_url");
    }

    public String getBotToken() {
        return System.getenv("bot_token");
    }

}
