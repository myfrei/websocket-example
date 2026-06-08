package com.bankdemo.auth.dto;

public record AuthResponse(
        String token,
        String username,
        String fullName,
        String phone,
        String role
) {
}
