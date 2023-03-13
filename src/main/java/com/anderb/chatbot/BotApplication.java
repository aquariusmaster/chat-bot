package com.anderb.chatbot;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.io.InputStream;
import java.io.OutputStream;

import static java.lang.System.getenv;

public class BotApplication implements RequestStreamHandler {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final AbsSender SENDER = new ChatBot(getenv("bot_username"), getenv("bot_token"), getenv("bot_url"));

    @Override
    public void handleRequest(InputStream input, OutputStream output, Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log("Parsing request");
        Update update = getUpdate(input);
        logger.log("Handling Update: " + update);
        handleUpdate(update);
    }

    private Update getUpdate(InputStream input) {
        try {
            return MAPPER.readValue(input, Update.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse update!", e);
        }
    }

    private void handleUpdate(Update update) {
        if (update == null || update.getMessage() == null) {
            return;
        }
        SendMessage sendMessage = SendMessage.builder()
                .chatId(update.getMessage().getChatId())
                .text("Echo> " + update.getMessage().getText())
                .build();
        try {
            SENDER.execute(sendMessage);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send message!", e);
        }
    }

}
