package com.anderb.chatbot;

import com.anderb.chatbot.model.ChatPrompt;
import com.anderb.chatbot.model.ChatResponse;
import com.anderb.chatbot.model.ErrorMessage;
import com.anderb.chatbot.model.Message;
import com.anderb.chatbot.model.functions.Function;
import com.anderb.chatbot.model.functions.FunctionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
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

@Slf4j
@AllArgsConstructor
public class ChatGptService {

    private final ObjectMapper mapper;
    private final DynamoDbChatHistoryClient chatHistoryClient;
    private final FunctionService functionService;

    public String callChat(Long chatId, String prompt) {
        List<Message> messages = chatHistoryClient.getChatMessages(chatId);
        messages.add(new Message("user", prompt));
        List<Function> functions = functionService.getFunctions();
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            var httpPost = prepareRequest(messages, functions);
            var response = httpclient.execute(httpPost);
            ChatResponse chatResponse = parseResponse(response);
            Message message = chatResponse.getChoices().get(0).getMessage();
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

    private void storeHistory(Long chatId, List<Message> messages) {
        int size = messages.size();
        if (size > Config.HISTORY_LENGTH) {
            messages = messages.subList(size - Config.HISTORY_LENGTH, size);
        }
        chatHistoryClient.putChatSession(chatId, messages);
    }

    private HttpUriRequest prepareRequest(List<Message> messages, List<Function> functions) throws JsonProcessingException {
        var prompt = new ChatPrompt(Config.AI_MODEL, messages, functions);
        var requestJson = mapper.writeValueAsString(prompt);
        log.debug("Request => {}", requestJson);
        return RequestBuilder.post(Config.OPENAI_API_URL)
                .addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + Config.OPENAI_API_KEY)
                .addHeader("Accept", "application/json")
                .setEntity(new StringEntity(requestJson, ContentType.APPLICATION_JSON))
                .build();
    }

    private ChatResponse parseResponse(CloseableHttpResponse response) throws IOException {
        var responseEntity = response.getEntity();
        var responseJson = IOUtils.toString(responseEntity.getContent(), StandardCharsets.UTF_8);
        log.debug("ChatGPT <= {}", responseJson);
        return mapper.readValue(responseJson, ChatResponse.class);
    }

}
