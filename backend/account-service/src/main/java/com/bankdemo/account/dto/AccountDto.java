package com.bankdemo.account.dto;

import com.bankdemo.account.domain.Account;

import java.math.BigDecimal;

public record AccountDto(
        String username,
        String fullName,
        String phone,
        String accountNumber,
        String currency,
        BigDecimal balance
) {
    public static AccountDto from(Account a) {
        return new AccountDto(a.getUsername(), a.getFullName(), a.getPhone(),
                a.getAccountNumber(), a.getCurrency(), a.getBalance());
    }
}
