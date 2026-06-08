package com.bankdemo.account.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class TransferCommandPublisher {

    private static final Logger log = LoggerFactory.getLogger(TransferCommandPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String topic;

    public TransferCommandPublisher(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${app.topics.transfer-commands}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void publish(TransferCommand command) {
        kafkaTemplate.send(topic, command.fromUsername(), command);
        log.info("Queued transfer {} from '{}' to '{}'",
                command.reference(), command.fromUsername(), command.toPhoneOrAccount());
    }
}
