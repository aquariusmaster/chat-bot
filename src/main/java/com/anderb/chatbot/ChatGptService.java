package com.anderb.chatbot;

import com.anderb.chatbot.model.ChatPrompt;
import com.anderb.chatbot.model.ChatResponse;
import com.anderb.chatbot.model.ErrorMessage;
import com.anderb.chatbot.model.Message;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.anderb.chatbot.Config.*;

@Slf4j
public class ChatGptService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static String callChat(Long chatId, String prompt) {
        List<Message> messages = getHistory(chatId);
        messages.add(new Message("user", prompt));
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            var httpPost = prepareRequest(messages);
            var response = httpclient.execute(httpPost);
            Message message = parseResponse(response);
            if (!(message instanceof ErrorMessage)) {
                messages.add(message);
                storeHistory(chatId, messages);
            }
            return message.getContent();
        } catch (Exception e) {
            var errorMsg = String.format("Chat call error: %s", e.getMessage());
            log.debug(errorMsg);
            return errorMsg;
        }
    }

    private static List<Message> getHistory(Long chatId) {
        return DynamoDbChatHistoryClient.getChatMessages(chatId);
    }

    private static void storeHistory(Long chatId, List<Message> messages) {
        while (messages.size() > HISTORY_LENGTH) {
            messages.remove(0);
        }
        DynamoDbChatHistoryClient.putChatSession(chatId, messages);
    }

    private static HttpUriRequest prepareRequest(List<Message> messages) throws JsonProcessingException {
        var prompt = new ChatPrompt(AI_MODEL, messages);
        var requestJson = MAPPER.writeValueAsString(prompt);
        log.debug("Request => {}", requestJson);
        return RequestBuilder.post(OPENAI_API_URL)
                .addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + OPENAI_API_KEY)
                .addHeader("Accept", "application/json")
                .setEntity(new StringEntity(requestJson, ContentType.APPLICATION_JSON))
                .build();
    }

    private static Message parseResponse(CloseableHttpResponse response) throws IOException {
        var responseEntity = response.getEntity();
        var responseJson = IOUtils.toString(responseEntity.getContent(), StandardCharsets.UTF_8);
        log.debug("ChatGPT <= {}", responseJson);
        if (response.getStatusLine().getStatusCode() >= 300) {
            var error = "Error: " + MAPPER.readTree(responseJson).get("error").get("message").asText();
            return new ErrorMessage(error);
        }
        ChatResponse chatResponse = MAPPER.readValue(responseJson, ChatResponse.class);
        return chatResponse.getChoices().get(0).getMessage();
    }

}
