package com.bankdemo.account.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transactions", indexes = {
        @Index(name = "idx_tx_owner_created", columnList = "owner_username, created_at")
})
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "owner_username", nullable = false)
    private String ownerUsername;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TxType type;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private String counterparty;

    @Column
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TxStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public Transaction() {
    }

    public Transaction(UUID accountId, String ownerUsername, TxType type, BigDecimal amount,
                       String currency, String counterparty, String description, TxStatus status) {
        this.accountId = accountId;
        this.ownerUsername = ownerUsername;
        this.type = type;
        this.amount = amount;
        this.currency = currency;
        this.counterparty = counterparty;
        this.description = description;
        this.status = status;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public TxType getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getCounterparty() {
        return counterparty;
    }

    public String getDescription() {
        return description;
    }

    public TxStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
