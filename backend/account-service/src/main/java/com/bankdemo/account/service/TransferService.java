package com.bankdemo.account.service;

import com.bankdemo.account.domain.Account;
import com.bankdemo.account.domain.Transaction;
import com.bankdemo.account.domain.TxStatus;
import com.bankdemo.account.domain.TxType;
import com.bankdemo.account.event.TransferCommand;
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
import java.util.Optional;

@Service
public class TransferService {

    private static final Logger log = LoggerFactory.getLogger(TransferService.class);

    private final AccountRepository accounts;
    private final TransactionRepository transactions;

    public TransferService(AccountRepository accounts, TransactionRepository transactions) {
        this.accounts = accounts;
        this.transactions = transactions;
    }

    @Transactional
    public void apply(TransferCommand cmd) {
        Account sender = accounts.findByUsername(cmd.fromUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sender account not found"));

        BigDecimal amount = cmd.amount();
        String target = cmd.toPhoneOrAccount().trim();

        if (!sender.getCurrency().equalsIgnoreCase(cmd.currency())) {
            recordFailure(sender, amount, cmd.currency(), target,
                    note(cmd, "Отклонено: счёт в " + sender.getCurrency()));
            log.warn("Transfer {} rejected: currency mismatch ({} != {})",
                    cmd.reference(), cmd.currency(), sender.getCurrency());
            return;
        }

        if (sender.getBalance().compareTo(amount) < 0) {
            recordFailure(sender, amount, sender.getCurrency(), target,
                    note(cmd, "Отклонено: недостаточно средств"));
            log.warn("Transfer {} rejected: insufficient funds for '{}'", cmd.reference(), sender.getUsername());
            return;
        }

        sender.setBalance(sender.getBalance().subtract(amount));
        sender.setUpdatedAt(Instant.now());
        accounts.save(sender);
        transactions.save(new Transaction(sender.getId(), sender.getUsername(), TxType.DEBIT,
                amount, sender.getCurrency(), target, cmd.description(), TxStatus.COMPLETED));

        Optional<Account> recipientOpt = accounts.findFirstByPhoneOrAccountNumber(target, target);
        if (recipientOpt.isPresent() && !recipientOpt.get().getId().equals(sender.getId())) {
            Account recipient = recipientOpt.get();
            recipient.setBalance(recipient.getBalance().add(amount));
            recipient.setUpdatedAt(Instant.now());
            accounts.save(recipient);
            transactions.save(new Transaction(recipient.getId(), recipient.getUsername(), TxType.CREDIT,
                    amount, recipient.getCurrency(), sender.getPhone(), cmd.description(), TxStatus.COMPLETED));
            log.info("Transfer {} applied: {} -> {} ({} {})",
                    cmd.reference(), sender.getUsername(), recipient.getUsername(), amount, sender.getCurrency());
        } else {
            log.info("Transfer {} applied: {} -> external {} ({} {})",
                    cmd.reference(), sender.getUsername(), target, amount, sender.getCurrency());
        }
    }

    private void recordFailure(Account sender, BigDecimal amount, String currency,
                               String target, String description) {
        transactions.save(new Transaction(sender.getId(), sender.getUsername(), TxType.DEBIT,
                amount, currency, target, description, TxStatus.FAILED));
    }

    private static String note(TransferCommand cmd, String reason) {
        return (cmd.description() == null || cmd.description().isBlank())
                ? reason
                : cmd.description() + " — " + reason;
    }
}
