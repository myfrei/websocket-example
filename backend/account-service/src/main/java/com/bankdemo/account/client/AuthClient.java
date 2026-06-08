package com.bankdemo.account.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AuthClient {

    private final RestClient restClient;

    public AuthClient(@Value("${app.auth-service-url}") String baseUrl,
                      @Value("${app.internal-api-key}") String apiKey) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("X-Internal-Api-Key", apiKey)
                .build();
    }

    public AuthUserDto getUser(String username) {
        return restClient.get()
                .uri("/internal/users/{username}", username)
                .retrieve()
                .body(AuthUserDto.class);
    }
}
