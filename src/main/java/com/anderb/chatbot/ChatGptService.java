package com.anderb.chatbot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
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
import java.util.Optional;
import java.util.function.Predicate;

import static com.anderb.chatbot.Config.*;

@Slf4j
public class ChatGptService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static String callChat(String messagePrompt) {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            var httpPost = prepareRequest(messagePrompt);
            var response = httpclient.execute(httpPost);
            return parseResponse(response);
        } catch (IOException e) {
            var errorMsg = String.format("Chat call error: %s", e.getMessage());
            log.debug(errorMsg);
            return errorMsg;
        }
    }

    private static HttpUriRequest prepareRequest(String messagePrompt) throws JsonProcessingException {
        var message = new Message("user", messagePrompt);
        var prompt = new ChatPrompt(AI_MODEL, List.of(message));
        var requestJson = MAPPER.writeValueAsString(prompt);
        log.debug("Request => {}", requestJson);
        StringEntity entity = new StringEntity(requestJson, ContentType.APPLICATION_JSON);
        return RequestBuilder.post(OPENAI_API_URL)
                .addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + OPENAI_API_KEY)
                .addHeader("Accept", "application/json")
                .setEntity(entity)
                .build();
    }

    private static String parseResponse(CloseableHttpResponse response) throws IOException {
        var responseEntity = response.getEntity();
        var responseJson = IOUtils.toString(responseEntity.getContent(), StandardCharsets.UTF_8);
        log.debug("ChatGPT <= {}", responseJson);
        if (response.getStatusLine().getStatusCode() >= 300) {
            var errorResponse = MAPPER.readValue(responseJson, ChatGPTErrorResponse.class);
            return errorResponse.getError().getMessage();
        }
        var chatResponse = MAPPER.readValue(responseJson, ChatGPTResponse.class);
        return Optional.ofNullable(chatResponse)
                .map(ChatGPTResponse::getChoices)
                .filter(Predicate.not(List::isEmpty))
                .map(choices -> choices.get(0))
                .map(Choice::getMessage)
                .map(Message::getContent)
                .orElseThrow(() -> new IOException("Invalid ChatGPT response"));
    }

    @Data
    @AllArgsConstructor
    private static class ChatPrompt {
        private String model;
        private List<Message> messages;

    }

    @Data
    @AllArgsConstructor
    private static class Message {

        private String role;
        private String content;

    }

    @Data
    private static class ChatGPTResponse {

        private List<Choice> choices;

    }

    @Data
    private static class Choice {

        private Message message;

    }

    @Data
    private static class ChatGPTErrorResponse {

        private ChatError error;

    }

    @Data
    private static class ChatError {

        private String message;
        private String type;
        private String param;
        private String code;

    }
}
