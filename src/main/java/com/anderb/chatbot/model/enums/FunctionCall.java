package com.anderb.chatbot.model.enums;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class FunctionCall {

    private Object value;

    public static FunctionCall auto() {
        return new FunctionCall("auto");
    }

    public static FunctionCall none() {
        return new FunctionCall("none");
    }

    public static FunctionCall function(String functionName) {
        return new FunctionCall();
    }

    private static class

}
