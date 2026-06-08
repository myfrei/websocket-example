package com.bankdemo.account.web;

import com.bankdemo.account.client.AuthClient;
import com.bankdemo.account.client.AuthUserDto;
import com.bankdemo.account.dto.AccountDto;
import com.bankdemo.account.dto.ManagerCreateAccountRequest;
import com.bankdemo.account.dto.TopUpRequest;
import com.bankdemo.account.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/manager/accounts")
public class ManagerController {

    private final AccountService accountService;
    private final AuthClient authClient;

    public ManagerController(AccountService accountService, AuthClient authClient) {
        this.accountService = accountService;
        this.authClient = authClient;
    }

    @PostMapping
    public AccountDto create(@Valid @RequestBody ManagerCreateAccountRequest req) {
        AuthUserDto user = authClient.getUser(req.username());
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + req.username());
        }
        return accountService.createAccount(user.username(), user.fullName(), user.phone(),
                req.currency(), req.initialBalance());
    }

    @PostMapping("/topup")
    public AccountDto topUp(@Valid @RequestBody TopUpRequest req) {
        return accountService.topUp(req.username(), req.amount(), req.description());
    }
}
