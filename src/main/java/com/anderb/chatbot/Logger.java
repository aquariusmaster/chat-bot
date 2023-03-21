package com.anderb.chatbot;

public class Logger {

    private static final boolean debugEnabled = Config.DEBUG;

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
