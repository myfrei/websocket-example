package com.bankdemo.account.dto;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record ManagerCreateAccountRequest(
        @NotBlank String username,
        String currency,
        BigDecimal initialBalance
) {
}
