package com.bankdemo.account.event;

import java.math.BigDecimal;

public record TransferCommand(
        String reference,
        String fromUsername,
        String toPhoneOrAccount,
        BigDecimal amount,
        String currency,
        String description
) {
}
