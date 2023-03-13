package com.anderb.chatbot;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Map;

public class BotApplication implements RequestHandler<Map<String,String>, String> {

    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public String handleRequest(Map<String,String> event, final Context context) {
        LambdaLogger logger = context.getLogger();
        // log execution details
        var envs = gson.toJson(System.getenv());
        logger.log("ENVIRONMENT VARIABLES: " + envs);
        var contextStr = gson.toJson(context);
        logger.log("CONTEXT: " + contextStr);
        // process event
        var eventStr = gson.toJson(event);
        logger.log("EVENT: " + eventStr);
        logger.log("EVENT TYPE: " + event.getClass().toString());
        return "200 OK: " + envs + ", context: " + contextStr + ", event: " + eventStr;
    }
}
