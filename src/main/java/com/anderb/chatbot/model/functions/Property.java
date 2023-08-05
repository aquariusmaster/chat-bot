package com.anderb.chatbot.model.functions;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class Property {

    private ParameterType type;
    private String name;
    private String description;
    @JsonProperty("enum")
    private List<String> values;
    private boolean required;

}
