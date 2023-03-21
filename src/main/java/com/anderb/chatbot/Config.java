package com.anderb.chatbot;

import java.util.Optional;

public class Config {
    public static final String BOT_USERNAME = System.getenv("BOT_USERNAME");
    public static final String BOT_TOKEN = System.getenv("BOT_TOKEN");
    public static final String BOT_URL = System.getenv("BOT_URL");
    public static final String ALLOWED_USERS = System.getenv("ALLOWED_USERS");
    public static final String AI_MODEL = System.getenv("AI_MODEL");
    public static final String OPENAI_API_URL = System.getenv("OPENAI_API_URL");
    public static final String OPENAI_API_KEY = System.getenv("OPENAI_API_KEY");

}
