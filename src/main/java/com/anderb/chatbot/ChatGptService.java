package com.anderb.chatbot;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
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

import static java.lang.System.getenv;

public class ChatGptService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static String callChat(String messagePrompt) {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            var httpPost = prepareRequest(messagePrompt);
            var response = httpclient.execute(httpPost);
            return parseResponse(response);
        } catch (IOException e) {
            var errorMsg = ChatGptService.class.getSimpleName() + " Chat call error: %s" + e.getMessage();
            Logger.error(errorMsg);
            return errorMsg;
        }
    }

    private static HttpUriRequest prepareRequest(String messagePrompt) throws JsonProcessingException {
        var message = new Message("user", messagePrompt);
        var prompt = new ChatPrompt("gpt-3.5-turbo", List.of(message));
        var requestJson = MAPPER.writeValueAsString(prompt);
        Logger.debug(ChatGptService.class.getSimpleName() + " Request => %s", requestJson);
        StringEntity entity = new StringEntity(requestJson, ContentType.APPLICATION_JSON);
        return RequestBuilder.post(getenv("OPENAI_API_URL"))
                .addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + getenv("OPENAI_API_KEY"))
                .addHeader("Accept", "application/json")
                .setEntity(entity)
                .build();
    }

    private static String parseResponse(CloseableHttpResponse response) throws IOException {
        var responseEntity = response.getEntity();
        var responseJson = new String(responseEntity.getContent().readAllBytes(), StandardCharsets.UTF_8);
        Logger.debug(ChatGptService.class.getSimpleName() + " ChatGPT <= %s", responseJson);
        if (response.getStatusLine().getStatusCode() >= 300) {
            var errorResponse = MAPPER.readValue(responseJson, ChatGPTErrorResponse.class);
            return errorResponse.getError().getMessage();
        }
        var chatResponse = MAPPER.readValue(responseJson, ChatGPTResponse.class);
        return chatResponse.getChoices().get(0).getMessage().getContent();
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class ChatPrompt {
        private String model;
        private List<Message> messages;

    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class Message {
        private String role;
        private String content;

    }

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class ChatGPTResponse {

        private List<Choice> choices;

    }

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Choice {

        private Message message;

    }

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class ChatGPTErrorResponse {

        private ChatError error;

    }

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class ChatError {
        private String message;
        private String type;
        private String param;
        private String code;
    }
}
