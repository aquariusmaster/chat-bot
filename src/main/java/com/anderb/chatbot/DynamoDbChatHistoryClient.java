package com.anderb.chatbot;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.anderb.chatbot.model.Message;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class DynamoDbChatHistoryClient {

    private static final AmazonDynamoDB DYNAMO_DB = AmazonDynamoDBClientBuilder.standard().build();
    private static final DynamoDB DB = new DynamoDB(DYNAMO_DB);

    public static List<Message> getChatMessages(Long chatId) {
        log.debug("Getting history for chat #{}", chatId);
        try {
            Table table = DB.getTable(Config.DYNAMO_TABLE_NAME);
            Item chat = table.getItem("chat_id", chatId);
            return Optional.ofNullable(chat)
                    .map(item -> item.getList("messages"))
                    .stream()
                    .flatMap(Collection::stream)
                    .map(DynamoDbChatHistoryClient::toMessage)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Cannot get history for chat #{}", chatId, e);
            return Collections.emptyList();
        }
    }

    public static void putChatSession(Long chatId, List<Message> messages) {
        try {
            List<Map<String, String>> stringMessages = messages.stream()
                    .map(message -> Map.of("role", message.getRole(), "content", message.getContent()))
                    .collect(Collectors.toList());
            Item item = new Item()
                    .withPrimaryKey("chat_id", chatId)
                    .withList("messages", stringMessages);
            Table table = DB.getTable(Config.DYNAMO_TABLE_NAME);
            table.putItem(item);
        } catch (Exception e) {
            log.error("Cannot update history for chat #{}", chatId, e);
        }
    }

    @SuppressWarnings("unchecked")
    private static Message toMessage(Object value) {
        Map<String, String> map = (Map<String, String>) value;
        return new Message(map.get("role"), map.get("content"));
    }

}
