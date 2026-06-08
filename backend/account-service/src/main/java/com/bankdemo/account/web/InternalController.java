package com.bankdemo.account.web;

import com.bankdemo.account.dto.AccountDto;
import com.bankdemo.account.dto.TransactionDto;
import com.bankdemo.account.event.TransferCommand;
import com.bankdemo.account.repo.AccountRepository;
import com.bankdemo.account.repo.TransactionRepository;
import com.bankdemo.account.service.TransferService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/internal")
public class InternalController {

    private final AccountRepository accounts;
    private final TransactionRepository transactions;
    private final TransferService transferService;

    public InternalController(AccountRepository accounts,
                             TransactionRepository transactions,
                             TransferService transferService) {
        this.accounts = accounts;
        this.transactions = transactions;
        this.transferService = transferService;
    }

    @GetMapping("/accounts/{username}")
    public AccountDto account(@PathVariable String username) {
        return accounts.findByUsername(username)
                .map(AccountDto::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
    }

    @GetMapping("/accounts/{username}/transactions")
    public List<TransactionDto> transactions(@PathVariable String username) {
        return transactions.findTop50ByOwnerUsernameOrderByCreatedAtDesc(username)
                .stream().map(TransactionDto::from).toList();
    }

    @PostMapping("/transfers/apply")
    public void applyTransfer(@RequestBody TransferCommand command) {
        transferService.apply(command);
    }
}
