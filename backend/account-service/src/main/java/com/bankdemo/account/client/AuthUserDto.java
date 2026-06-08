package com.bankdemo.account.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AuthUserDto(
        String username,
        String fullName,
        String phone,
        String role,
        String status
) {
}
