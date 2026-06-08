package com.bankdemo.ws.kafka;

import com.bankdemo.ws.ws.SessionRegistry;
import com.bankdemo.ws.ws.WebSocketMessenger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TransactionCdcListener {

    private static final Logger log = LoggerFactory.getLogger(TransactionCdcListener.class);

    private final SessionRegistry registry;
    private final WebSocketMessenger messenger;

    public TransactionCdcListener(SessionRegistry registry, WebSocketMessenger messenger) {
        this.registry = registry;
        this.messenger = messenger;
    }

    @KafkaListener(topics = "${app.topics.transactions}")
    public void onChange(TransactionChange change) {
        if (change == null || change.ownerUsername() == null) {
            return;
        }
        TransactionEvent event = new TransactionEvent(
                change.id(),
                change.type(),
                change.amount(),
                change.currency(),
                change.counterparty(),
                change.description(),
                change.status(),
                change.createdAt());
        messenger.broadcast(registry.forUser(change.ownerUsername()), "transaction", event);
        log.info("Pushed {} {} {} to '{}'", event.type(), event.amount(), event.currency(), change.ownerUsername());
    }
}
