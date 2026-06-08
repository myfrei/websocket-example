package com.bankdemo.chat.dto;

import com.bankdemo.chat.domain.Chat;

import java.time.Instant;

public record ChatDto(
        String chatId,
        String user,
        String status,
        Instant createdAt,
        Instant updatedAt
) {
    public static ChatDto from(Chat chat) {
        return new ChatDto(
                chat.getId().toString(),
                chat.getUserUsername(),
                chat.getStatus().name(),
                chat.getCreatedAt(),
                chat.getUpdatedAt());
    }
}
