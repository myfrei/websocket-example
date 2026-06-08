package com.bankdemo.account.web;

import com.bankdemo.account.dto.AccountDto;
import com.bankdemo.account.dto.TransactionDto;
import com.bankdemo.account.repo.AccountRepository;
import com.bankdemo.account.repo.TransactionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountRepository accounts;
    private final TransactionRepository transactions;

    public AccountController(AccountRepository accounts, TransactionRepository transactions) {
        this.accounts = accounts;
        this.transactions = transactions;
    }

    @GetMapping("/me")
    public AccountDto me(Authentication authentication) {
        String username = authentication.getName();
        return accounts.findByUsername(username)
                .map(AccountDto::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Account is still being provisioned"));
    }

    @GetMapping("/me/transactions")
    public List<TransactionDto> myTransactions(Authentication authentication) {
        return transactions
                .findTop50ByOwnerUsernameOrderByCreatedAtDesc(authentication.getName())
                .stream()
                .map(TransactionDto::from)
                .toList();
    }
}
