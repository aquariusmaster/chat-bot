package com.anderb.chatbot.model.functions;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Function {

    private String name;
    private String description;
    private Parameters parameters;

}
