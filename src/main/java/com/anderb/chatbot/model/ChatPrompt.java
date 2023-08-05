package com.anderb.chatbot.model;

import com.anderb.chatbot.model.functions.Function;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatPrompt {

    private String model;
    private List<Message> messages;
    private List<Function> functions;
    private Object functionCall = "auto";

    public ChatPrompt(String model, List<Message> messages) {
        this.model = model;
        this.messages = messages;
    }

    public ChatPrompt(String model, List<Message> messages, List<Function> functions) {
        this.model = model;
        this.messages = messages;
        this.functions = functions;
    }

}
