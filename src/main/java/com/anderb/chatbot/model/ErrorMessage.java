package com.anderb.chatbot.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ErrorMessage extends Message {
    public ErrorMessage(String content) {
        super(null, content);
    }
}
