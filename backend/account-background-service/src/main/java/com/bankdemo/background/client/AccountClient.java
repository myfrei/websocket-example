package com.bankdemo.background.client;

import com.bankdemo.background.event.TransferCommand;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
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

    public void applyTransfer(TransferCommand command) {
        restClient.post()
                .uri("/internal/transfers/apply")
                .contentType(MediaType.APPLICATION_JSON)
                .body(command)
                .retrieve()
                .toBodilessEntity();
    }
}
