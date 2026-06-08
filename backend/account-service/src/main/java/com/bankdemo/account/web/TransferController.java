package com.bankdemo.account.web;

import com.bankdemo.account.dto.TransferAccepted;
import com.bankdemo.account.dto.TransferRequest;
import com.bankdemo.account.event.TransferCommand;
import com.bankdemo.account.event.TransferCommandPublisher;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {

    private final TransferCommandPublisher publisher;

    public TransferController(TransferCommandPublisher publisher) {
        this.publisher = publisher;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public TransferAccepted transfer(@Valid @RequestBody TransferRequest req, Authentication authentication) {
        String reference = UUID.randomUUID().toString();
        publisher.publish(new TransferCommand(
                reference,
                authentication.getName(),
                req.toPhoneOrAccount(),
                req.amount(),
                req.currency(),
                req.description()));
        return new TransferAccepted(reference, "PENDING");
    }
}
