package com.bankdemo.auth.dto;

import com.bankdemo.auth.domain.User;

public record UserDto(
        String username,
        String fullName,
        String phone,
        String role,
        String status
) {
    public static UserDto from(User user) {
        return new UserDto(
                user.getUsername(),
                user.getFullName(),
                user.getPhone(),
                user.getRole().name(),
                user.getStatus().name());
    }
}
