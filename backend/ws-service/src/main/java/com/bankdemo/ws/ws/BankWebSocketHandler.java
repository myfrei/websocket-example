package com.bankdemo.ws.ws;

import com.bankdemo.ws.client.AccountClient;
import com.bankdemo.ws.client.ChatClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class BankWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(BankWebSocketHandler.class);

    private final SessionRegistry registry;
    private final WebSocketMessenger messenger;
    private final AccountClient accountClient;
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public BankWebSocketHandler(SessionRegistry registry, WebSocketMessenger messenger,
                                AccountClient accountClient, ChatClient chatClient,
                                ObjectMapper objectMapper) {
        this.registry = registry;
        this.messenger = messenger;
        this.accountClient = accountClient;
        this.chatClient = chatClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String username = attr(session, "username");
        String role = attr(session, "role");
        registry.register(username, role, session);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("username", username);
        data.put("role", role);
        data.put("commands", new String[]{"getBalance", "getTransactions", "createChat",
                "closeChat", "sendMessage", "getMessages", "getChats", "ping"});
        messenger.send(session, "connected", data);
        log.info("WS connected: '{}' ({})", username, role);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String username = attr(session, "username");
        String role = attr(session, "role");

        JsonNode node;
        try {
            node = objectMapper.readTree(message.getPayload());
        } catch (Exception ex) {
            messenger.error(session, "Invalid JSON");
            return;
        }

        String command = node.path("command").asText(null);
        if (command == null || command.isBlank()) {
            messenger.error(session, "Missing 'command'");
            return;
        }

        try {
            dispatch(session, username, role, command, node);
        } catch (RestClientResponseException ex) {
            messenger.error(session, "Сервис вернул " + ex.getStatusCode().value());
        } catch (Exception ex) {
            log.warn("Command '{}' failed: {}", command, ex.getMessage());
            messenger.error(session, "Ошибка обработки команды: " + command);
        }
    }

    private void dispatch(WebSocketSession session, String username, String role,
                          String command, JsonNode node) {
        boolean manager = "MANAGER".equals(role);
        switch (command) {
            case "ping" -> messenger.send(session, "pong", null);

            case "getBalance" -> {
                String target = manager ? node.path("username").asText(null) : username;
                if (target == null) {
                    messenger.error(session, "username required");
                    return;
                }
                try {
                    messenger.send(session, "balance", accountClient.getBalance(target));
                } catch (RestClientResponseException ex) {
                    if (ex.getStatusCode().value() == 404) {
                        messenger.send(session, "noAccount", null);
                    } else {
                        throw ex;
                    }
                }
            }

            case "getTransactions" -> {
                String target = manager ? node.path("username").asText(null) : username;
                if (target == null) {
                    messenger.error(session, "username required");
                    return;
                }
                try {
                    messenger.send(session, "transactions", accountClient.getTransactions(target));
                } catch (RestClientResponseException ex) {
                    if (ex.getStatusCode().value() == 404) {
                        messenger.send(session, "transactions", List.of());
                    } else {
                        throw ex;
                    }
                }
            }

            case "createChat" -> {
                String chatUser = manager ? node.path("chatUser").asText(null) : username;
                if (chatUser == null) {
                    messenger.error(session, "chatUser required");
                    return;
                }
                JsonNode chat = chatClient.open(chatUser);
                messenger.send(session, "chatOpened", chat);
                messenger.broadcast(registry.managerSessions(), "chatOpened", chat);
            }

            case "closeChat" -> {
                String chatUser = manager ? node.path("chatUser").asText(null) : username;
                if (chatUser == null) {
                    messenger.error(session, "chatUser required");
                    return;
                }
                JsonNode chat = chatClient.close(chatUser);
                messenger.send(session, "chatClosed", chat);
                messenger.broadcast(registry.managerSessions(), "chatClosed", chat);
            }

            case "getMessages" -> {
                String chatUser = manager ? node.path("chatUser").asText(null) : username;
                if (chatUser == null) {
                    messenger.error(session, "chatUser required");
                    return;
                }
                messenger.send(session, "messages", chatClient.listMessages(chatUser));
            }

            case "getChats" -> {
                if (!manager) {
                    messenger.error(session, "Only managers can list chats");
                    return;
                }
                messenger.send(session, "chats", chatClient.listChats());
            }

            case "sendMessage" -> {
                String text = node.path("text").asText(null);
                if (text == null || text.isBlank()) {
                    messenger.error(session, "text required");
                    return;
                }
                String chatUser = manager ? node.path("chatUser").asText(null) : username;
                if (chatUser == null) {
                    messenger.error(session, "chatUser required");
                    return;
                }
                chatClient.postMessage(chatUser, username, role, text);
            }

            default -> messenger.error(session, "Unknown command: " + command);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        registry.unregister(session);
        log.info("WS closed: '{}'", attr(session, "username"));
    }

    private static String attr(WebSocketSession session, String key) {
        Object value = session.getAttributes().get(key);
        return value == null ? null : value.toString();
    }
}
