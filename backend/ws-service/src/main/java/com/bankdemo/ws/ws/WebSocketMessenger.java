package com.bankdemo.ws.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class WebSocketMessenger {

    private static final Logger log = LoggerFactory.getLogger(WebSocketMessenger.class);

    private final ObjectMapper objectMapper;

    public WebSocketMessenger(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void send(WebSocketSession session, String event, Object data) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("event", event);
        if (data != null) {
            payload.put("data", data);
        }
        write(session, payload);
    }

    public void error(WebSocketSession session, String message) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("event", "error");
        payload.put("message", message);
        write(session, payload);
    }

    public void broadcast(Collection<WebSocketSession> sessions, String event, Object data) {
        for (WebSocketSession session : sessions) {
            send(session, event, data);
        }
    }

    private void write(WebSocketSession session, Object payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            synchronized (session) {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(json));
                }
            }
        } catch (Exception ex) {
            log.warn("Failed to send WebSocket message: {}", ex.getMessage());
        }
    }
}
