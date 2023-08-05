package com.anderb.chatbot.model;

import com.anderb.chatbot.model.enums.FinishReason;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Choice {

    private int index;
    private Message message;
    private FinishReason finishReason;

    @JsonCreator
    public static FinishReason fromString(@JsonProperty("finishReason") String finishReason) {
        return FinishReason.valueOf(finishReason.toUpperCase());
    }

}