package com.bankdemo.ws.kafka;

public record TransactionEvent(
        String id,
        String type,
        String amount,
        String currency,
        String counterparty,
        String description,
        String status,
        String createdAt
) {
}
