package com.bankdemo.chat.dto;

import jakarta.validation.constraints.NotBlank;

public record PostMessageRequest(
        @NotBlank String fromUsername,
        @NotBlank String fromRole,
        @NotBlank String text
) {
}
