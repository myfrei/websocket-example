package com.bankdemo.auth.dto;

public record RegisterResponse(
        String username,
        String status,
        String message
) {
}
