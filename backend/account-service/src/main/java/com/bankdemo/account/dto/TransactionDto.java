package com.bankdemo.account.dto;

import com.bankdemo.account.domain.Transaction;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionDto(
        String id,
        String type,
        BigDecimal amount,
        String currency,
        String counterparty,
        String description,
        String status,
        Instant createdAt
) {
    public static TransactionDto from(Transaction t) {
        return new TransactionDto(
                t.getId().toString(),
                t.getType().name(),
                t.getAmount(),
                t.getCurrency(),
                t.getCounterparty(),
                t.getDescription(),
                t.getStatus().name(),
                t.getCreatedAt());
    }
}
