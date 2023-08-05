package com.anderb.chatbot.model.functions;

import lombok.AllArgsConstructor;

import java.util.Locale;

@AllArgsConstructor
public enum ParameterType {

    OBJECT,
    STRING,
    NUMBER,
    ARRAY;

    public String getName() {
        return name().toLowerCase(Locale.ROOT);
    }

}
