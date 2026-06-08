package com.bankdemo.ws.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AccountClient {

    private final RestClient restClient;

    public AccountClient(@Value("${app.account-service-url}") String baseUrl,
                         @Value("${app.internal-api-key}") String apiKey) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("X-Internal-Api-Key", apiKey)
                .build();
    }

    public JsonNode getBalance(String username) {
        return restClient.get()
                .uri("/internal/accounts/{username}", username)
                .retrieve()
                .body(JsonNode.class);
    }

    public JsonNode getTransactions(String username) {
        return restClient.get()
                .uri("/internal/accounts/{username}/transactions", username)
                .retrieve()
                .body(JsonNode.class);
    }
}
