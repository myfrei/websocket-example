package com.bankdemo.background.listener;

import com.bankdemo.background.client.AccountClient;
import com.bankdemo.background.event.TransferCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TransferCommandListener {

    private static final Logger log = LoggerFactory.getLogger(TransferCommandListener.class);

    private final AccountClient accountClient;

    public TransferCommandListener(AccountClient accountClient) {
        this.accountClient = accountClient;
    }

    @KafkaListener(topics = "${app.topics.transfer-commands}")
    public void onTransferCommand(TransferCommand command) {
        log.info("Applying transfer {} from '{}' to '{}'",
                command.reference(), command.fromUsername(), command.toPhoneOrAccount());
        accountClient.applyTransfer(command);
    }
}
