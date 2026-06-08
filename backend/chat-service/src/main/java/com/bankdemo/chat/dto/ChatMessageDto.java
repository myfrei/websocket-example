package com.bankdemo.chat.dto;

import com.bankdemo.chat.domain.ChatMessage;

import java.time.Instant;

public record ChatMessageDto(
        String id,
        String chatId,
        String fromUsername,
        String fromRole,
        String text,
        Instant createdAt
) {
    public static ChatMessageDto from(ChatMessage message) {
        return new ChatMessageDto(
                message.getId().toString(),
                message.getChatId().toString(),
                message.getFromUsername(),
                message.getFromRole(),
                message.getText(),
                message.getCreatedAt());
    }
}
