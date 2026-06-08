package com.bankdemo.ws.kafka;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ChatMessageEvent(
        String chatId,
        String chatUser,
        String fromUsername,
        String fromRole,
        String text,
        String createdAt
) {
}
