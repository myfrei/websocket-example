package com.bankdemo.account.service;

import com.bankdemo.account.domain.Account;
import com.bankdemo.account.domain.Transaction;
import com.bankdemo.account.domain.TxStatus;
import com.bankdemo.account.domain.TxType;
import com.bankdemo.account.dto.AccountDto;
import com.bankdemo.account.repo.AccountRepository;
import com.bankdemo.account.repo.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    private final AccountRepository accounts;
    private final TransactionRepository transactions;

    public AccountService(AccountRepository accounts, TransactionRepository transactions) {
        this.accounts = accounts;
        this.transactions = transactions;
    }

    @Transactional
    public AccountDto createAccount(String username, String fullName, String phone,
                                    String currency, BigDecimal initialBalance) {
        if (accounts.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Account already exists for " + username);
        }
        BigDecimal balance = initialBalance == null ? BigDecimal.ZERO : initialBalance;
        String cur = (currency == null || currency.isBlank()) ? "RUB" : currency.toUpperCase();
        Instant now = Instant.now();

        Account account = new Account();
        account.setUsername(username);
        account.setFullName(fullName);
        account.setPhone(phone);
        account.setAccountNumber(generateAccountNumber());
        account.setCurrency(cur);
        account.setBalance(balance);
        account.setCreatedAt(now);
        account.setUpdatedAt(now);
        accounts.save(account);

        if (balance.signum() > 0) {
            transactions.save(new Transaction(account.getId(), username, TxType.CREDIT,
                    balance, cur, "MANAGER", "Открытие счёта", TxStatus.COMPLETED));
        }
        log.info("Manager created account {} for '{}' with balance {} {}",
                account.getAccountNumber(), username, balance, cur);
        return AccountDto.from(account);
    }

    @Transactional
    public AccountDto topUp(String username, BigDecimal amount, String description) {
        Account account = accounts.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found for " + username));
        account.setBalance(account.getBalance().add(amount));
        account.setUpdatedAt(Instant.now());
        accounts.save(account);

        String note = (description == null || description.isBlank()) ? "Пополнение менеджером" : description;
        transactions.save(new Transaction(account.getId(), username, TxType.CREDIT,
                amount, account.getCurrency(), "MANAGER", note, TxStatus.COMPLETED));
        log.info("Manager topped up '{}' by {} {}", username, amount, account.getCurrency());
        return AccountDto.from(account);
    }

    private String generateAccountNumber() {
        for (int attempt = 0; attempt < 10; attempt++) {
            String candidate = "ACC" + String.format("%010d",
                    ThreadLocalRandom.current().nextLong(10_000_000_000L));
            if (!accounts.existsByAccountNumber(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Could not allocate a unique account number");
    }
}
