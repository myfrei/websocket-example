package com.bankdemo.account.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TopUpRequest(
        @NotBlank String username,
        @NotNull @DecimalMin(value = "0.01", message = "amount must be positive") BigDecimal amount,
        String description
) {
}
