package com.bankdemo.account.seed;

import com.bankdemo.account.domain.Account;
import com.bankdemo.account.domain.Transaction;
import com.bankdemo.account.domain.TxStatus;
import com.bankdemo.account.domain.TxType;
import com.bankdemo.account.repo.AccountRepository;
import com.bankdemo.account.repo.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Component
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final AccountRepository accounts;
    private final TransactionRepository transactions;

    public DataSeeder(AccountRepository accounts, TransactionRepository transactions) {
        this.accounts = accounts;
        this.transactions = transactions;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seed("alice", "Алиса Иванова", "+79990000001", "ACC1000000001", new BigDecimal("50000.00"));
        seed("bob", "Борис Петров", "+79990000002", "ACC1000000002", new BigDecimal("30000.00"));
    }

    private void seed(String username, String fullName, String phone, String accountNumber, BigDecimal balance) {
        if (accounts.existsByUsername(username)) {
            return;
        }
        Instant now = Instant.now();
        Account account = new Account();
        account.setUsername(username);
        account.setFullName(fullName);
        account.setPhone(phone);
        account.setAccountNumber(accountNumber);
        account.setCurrency("RUB");
        account.setBalance(balance);
        account.setCreatedAt(now);
        account.setUpdatedAt(now);
        accounts.save(account);

        transactions.save(new Transaction(account.getId(), username, TxType.CREDIT,
                balance, "RUB", "BANK", "Открытие счёта", TxStatus.COMPLETED));

        log.info("Seeded demo account {} for '{}'", accountNumber, username);
    }
}
