package com.anderb.chatbot;

import java.util.Optional;

import static java.lang.System.getenv;

public class Logger {

    private static final boolean debugEnabled;

    static {
        debugEnabled = Optional.ofNullable(getenv("DEBUG"))
                .filter(Boolean::parseBoolean)
                .isPresent();
    }

    public static void info(String log, Object... args) {
        System.out.printf((log) + "%n", args);
    }

    public static void debug(String log, Object... args) {
        if (debugEnabled) {
            System.out.printf((log) + "%n", args);
        }
    }

    public static void error(String log, Object... args) {
        System.out.printf((log) + "%n", args);
    }

}
