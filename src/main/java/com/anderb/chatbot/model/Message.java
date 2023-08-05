package com.anderb.chatbot.model;

import com.anderb.chatbot.model.functions.FunctionCall;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    private String role;
    private String content;
    private FunctionCall functionCall;

    public Message(String role, String content) {
        this.role = role;
        this.content = content;
    }

}
