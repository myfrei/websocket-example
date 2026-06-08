package com.bankdemo.chat.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "chat_messages", indexes = {
        @Index(name = "idx_msg_chat_created", columnList = "chat_id, created_at")
})
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "chat_id", nullable = false)
    private UUID chatId;

    @Column(name = "from_username", nullable = false)
    private String fromUsername;

    @Column(name = "from_role", nullable = false)
    private String fromRole;

    @Column(nullable = false, length = 2000)
    private String text;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public ChatMessage() {
    }

    public ChatMessage(UUID chatId, String fromUsername, String fromRole, String text) {
        this.chatId = chatId;
        this.fromUsername = fromUsername;
        this.fromRole = fromRole;
        this.text = text;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getChatId() {
        return chatId;
    }

    public String getFromUsername() {
        return fromUsername;
    }

    public String getFromRole() {
        return fromRole;
    }

    public String getText() {
        return text;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
