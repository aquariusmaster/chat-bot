package com.anderb.chatbot;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.anderb.chatbot.model.Message;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class DynamoDbChatHistoryClient {

    private final DynamoDB db;
    private final String historyTableName;

    public DynamoDbChatHistoryClient() {
        AmazonDynamoDB dynamoDB = AmazonDynamoDBClientBuilder.standard().build();
        db = new DynamoDB(dynamoDB);
        historyTableName = Config.DYNAMO_TABLE_NAME;
    }

    public List<Message> getChatMessages(Long chatId) {
        log.debug("Getting history for chat #{}", chatId);
        try {
            Table table = db.getTable(historyTableName);
            return Optional.ofNullable(table.getItem("chat_id", chatId))
                    .filter(this::isSessionNotExpired)
                    .map(item -> item.getList("messages"))
                    .stream()
                    .flatMap(Collection::stream)
                    .map(this::toMessage)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Cannot get history for chat #{}", chatId, e);
            return new ArrayList<>();
        }
    }

    public void putChatSession(Long chatId, List<Message> messages) {
        try {
            List<Map<String, String>> stringMessages = messages.stream()
                    .map(message -> Map.of("role", message.getRole(), "content", message.getContent()))
                    .collect(Collectors.toList());
            Item item = new Item()
                    .withPrimaryKey("chat_id", chatId)
                    .withList("messages", stringMessages)
                    .withString("last_interaction", LocalDateTime.now().toString());
            Table table = db.getTable(historyTableName);
            table.putItem(item);
        } catch (Exception e) {
            log.error("Cannot update history for chat #{}", chatId, e);
        }
    }

    @SuppressWarnings("unchecked")
    private Message toMessage(Object value) {
        Map<String, String> map = (Map<String, String>) value;
        return new Message(map.get("role"), map.get("content"));
    }

    private boolean isSessionNotExpired(Item item) {
        int sessionMaxLifetime = Config.SESSION_MAX_LIFETIME;
        if (sessionMaxLifetime == -1) {
            return true;
        }
        LocalDateTime minutesAgo = LocalDateTime.now().minusMinutes(sessionMaxLifetime);
        LocalDateTime lastInteraction = LocalDateTime.parse(item.getString("last_interaction"));
        return lastInteraction.isAfter(minutesAgo);
    }

}
