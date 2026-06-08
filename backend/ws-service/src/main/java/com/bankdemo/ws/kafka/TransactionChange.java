package com.bankdemo.ws.kafka;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TransactionChange(
        String id,
        @JsonProperty("owner_username") String ownerUsername,
        String type,
        String amount,
        String currency,
        String counterparty,
        String description,
        String status,
        @JsonProperty("created_at") String createdAt
) {
}
