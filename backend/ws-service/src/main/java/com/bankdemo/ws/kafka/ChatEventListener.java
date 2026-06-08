package com.bankdemo.ws.kafka;

import com.bankdemo.ws.ws.SessionRegistry;
import com.bankdemo.ws.ws.WebSocketMessenger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashSet;
import java.util.Set;

@Component
public class ChatEventListener {

    private static final Logger log = LoggerFactory.getLogger(ChatEventListener.class);

    private final SessionRegistry registry;
    private final WebSocketMessenger messenger;

    public ChatEventListener(SessionRegistry registry, WebSocketMessenger messenger) {
        this.registry = registry;
        this.messenger = messenger;
    }

    @KafkaListener(topics = "${app.topics.chat-events}", containerFactory = "chatKafkaListenerContainerFactory")
    public void onChatEvent(ChatMessageEvent event) {
        if (event == null || event.chatUser() == null) {
            return;
        }
        Set<WebSocketSession> targets = new HashSet<>(registry.forUser(event.chatUser()));
        targets.addAll(registry.managerSessions());
        messenger.broadcast(targets, "chatMessage", event);
        log.info("Delivered chat message for '{}' to {} session(s)", event.chatUser(), targets.size());
    }
}
