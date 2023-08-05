package com.anderb.chatbot.model.enums;

import java.util.Locale;

public enum FinishReason {

    STOP,
    FUNCTION_CALL;

    public String getName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
