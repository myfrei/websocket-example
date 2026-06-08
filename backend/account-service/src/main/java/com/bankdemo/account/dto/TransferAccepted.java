package com.bankdemo.account.dto;

public record TransferAccepted(
        String reference,
        String status
) {
}
