package com.bankdemo.ws.ws;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionRegistry {

    private final Map<String, Set<WebSocketSession>> byUser = new ConcurrentHashMap<>();
    private final Set<WebSocketSession> managers = ConcurrentHashMap.newKeySet();

    public void register(String username, String role, WebSocketSession session) {
        byUser.computeIfAbsent(username, key -> ConcurrentHashMap.newKeySet()).add(session);
        if ("MANAGER".equals(role)) {
            managers.add(session);
        }
    }

    public void unregister(WebSocketSession session) {
        byUser.values().forEach(set -> set.remove(session));
        managers.remove(session);
    }

    public Set<WebSocketSession> forUser(String username) {
        return byUser.getOrDefault(username, Set.of());
    }

    public Set<WebSocketSession> managerSessions() {
        return managers;
    }
}
