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
    public static final String DYNAMO_TABLE_NAME = System.getenv("DYNAMO_TABLE_NAME");
    public static final int HISTORY_LENGTH = getAsInt("HISTORY_LENGTH", 0);
    public static final int SESSION_MAX_LIFETIME = getAsInt("SESSION_MAX_LIFETIME", -1);

    private static int getAsInt(String varName, int defaultValue) {
        return Optional.ofNullable(System.getenv(varName))
                .map(Integer::valueOf)
                .orElse(defaultValue);
    }

}
