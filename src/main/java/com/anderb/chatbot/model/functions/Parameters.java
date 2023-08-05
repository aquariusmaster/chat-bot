package com.anderb.chatbot.model.functions;

import lombok.Getter;

import java.util.Collections;
import java.util.List;

@Getter
public class Parameters {

    private ParameterType type;
    private List<Property> properties;

    public List<String> getRequired() {
        if (properties == null) {
            return Collections.emptyList();
        }
        return properties.stream()
                .filter(Property::isRequired)
                .map(Property::getName)
                .toList();
    }

}
