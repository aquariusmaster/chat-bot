package com.anderb.chatbot.model.functions;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FunctionCall {

    private String name;
    private String arguments;

}
