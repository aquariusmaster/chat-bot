package com.anderb.chatbot.model.functions;

import java.util.List;

public interface FunctionService {

    List<Function> getFunctions();
    String invokeFunction(String functionName);

}
