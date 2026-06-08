package com.bankdemo.chat.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ChatEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(ChatEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String topic;

    public ChatEventPublisher(KafkaTemplate<String, Object> kafkaTemplate,
                              @Value("${app.topics.chat-events}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void publish(ChatMessageEvent event) {
        kafkaTemplate.send(topic, event.chatUser(), event);
        log.info("Published chat message for chat '{}' from '{}'", event.chatUser(), event.fromUsername());
    }
}
