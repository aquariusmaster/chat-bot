package com.anderb.chatbot;

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
        String requestJson = MAPPER.createObjectNode()
                .put("model", AI_MODEL)
                .set("messages", MAPPER.createArrayNode()
                        .add(MAPPER.createObjectNode()
                                .put("role", "user")
                                .put("content", messagePrompt)
                        )
                )
                .toString();
        log.debug("Request => {}", requestJson);
        return RequestBuilder.post(OPENAI_API_URL)
                .addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + OPENAI_API_KEY)
                .addHeader("Accept", "application/json")
                .setEntity(new StringEntity(requestJson, ContentType.APPLICATION_JSON))
                .build();
    }

    private static String parseResponse(CloseableHttpResponse response) throws IOException {
        var responseEntity = response.getEntity();
        var responseJson = IOUtils.toString(responseEntity.getContent(), StandardCharsets.UTF_8);
        log.debug("ChatGPT <= {}", responseJson);
        if (response.getStatusLine().getStatusCode() >= 300) {
            return "Error: " + MAPPER.readTree(responseJson).get("error").get("message").asText();
        }
        return MAPPER.readTree(responseJson).get("choices").get(0).get("message").get("content").asText();
    }

}
