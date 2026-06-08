package com.bankdemo.background.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TransferCommand(
        String reference,
        String fromUsername,
        String toPhoneOrAccount,
        BigDecimal amount,
        String currency,
        String description
) {
}
