package com.bankdemo.ws.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class ChatClient {

    private final RestClient restClient;

    public ChatClient(@Value("${app.chat-service-url}") String baseUrl,
                      @Value("${app.internal-api-key}") String apiKey) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("X-Internal-Api-Key", apiKey)
                .build();
    }

    public JsonNode open(String username) {
        return restClient.post()
                .uri("/internal/chats/{username}/open", username)
                .retrieve()
                .body(JsonNode.class);
    }

    public JsonNode close(String username) {
        return restClient.post()
                .uri("/internal/chats/{username}/close", username)
                .retrieve()
                .body(JsonNode.class);
    }

    public void postMessage(String username, String fromUsername, String fromRole, String text) {
        restClient.post()
                .uri("/internal/chats/{username}/messages", username)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("fromUsername", fromUsername, "fromRole", fromRole, "text", text))
                .retrieve()
                .toBodilessEntity();
    }

    public JsonNode listMessages(String username) {
        return restClient.get()
                .uri("/internal/chats/{username}/messages", username)
                .retrieve()
                .body(JsonNode.class);
    }

    public JsonNode listChats() {
        return restClient.get()
                .uri("/internal/chats")
                .retrieve()
                .body(JsonNode.class);
    }
}
