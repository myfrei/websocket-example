package com.bankdemo.chat.event;

public record ChatMessageEvent(
        String chatId,
        String chatUser,
        String fromUsername,
        String fromRole,
        String text,
        String createdAt
) {
}
