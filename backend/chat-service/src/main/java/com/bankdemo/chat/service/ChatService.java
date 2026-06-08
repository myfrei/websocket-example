package com.bankdemo.chat.service;

import com.bankdemo.chat.domain.Chat;
import com.bankdemo.chat.domain.ChatMessage;
import com.bankdemo.chat.domain.ChatStatus;
import com.bankdemo.chat.dto.ChatDto;
import com.bankdemo.chat.dto.ChatMessageDto;
import com.bankdemo.chat.event.ChatEventPublisher;
import com.bankdemo.chat.event.ChatMessageEvent;
import com.bankdemo.chat.repo.ChatMessageRepository;
import com.bankdemo.chat.repo.ChatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class ChatService {

    private final ChatRepository chatRepo;
    private final ChatMessageRepository messageRepo;
    private final ChatEventPublisher publisher;

    public ChatService(ChatRepository chatRepo, ChatMessageRepository messageRepo, ChatEventPublisher publisher) {
        this.chatRepo = chatRepo;
        this.messageRepo = messageRepo;
        this.publisher = publisher;
    }

    @Transactional
    public ChatDto open(String username) {
        Chat chat = chatRepo.findByUserUsername(username).orElseGet(() -> newChat(username));
        chat.setStatus(ChatStatus.OPEN);
        chat.setUpdatedAt(Instant.now());
        chatRepo.save(chat);
        return ChatDto.from(chat);
    }

    @Transactional
    public ChatDto close(String username) {
        Chat chat = chatRepo.findByUserUsername(username).orElseGet(() -> newChat(username));
        chat.setStatus(ChatStatus.CLOSED);
        chat.setUpdatedAt(Instant.now());
        chatRepo.save(chat);
        return ChatDto.from(chat);
    }

    @Transactional
    public ChatMessageDto postMessage(String username, String fromUsername, String fromRole, String text) {
        Chat chat = chatRepo.findByUserUsername(username).orElseGet(() -> newChat(username));
        chat.setStatus(ChatStatus.OPEN);
        chat.setUpdatedAt(Instant.now());
        chatRepo.save(chat);

        ChatMessage message = messageRepo.save(new ChatMessage(chat.getId(), fromUsername, fromRole, text));
        publisher.publish(new ChatMessageEvent(chat.getId().toString(), username, fromUsername, fromRole,
                text, message.getCreatedAt().toString()));
        return ChatMessageDto.from(message);
    }

    @Transactional(readOnly = true)
    public List<ChatMessageDto> listMessages(String username) {
        return chatRepo.findByUserUsername(username)
                .map(chat -> messageRepo.findByChatIdOrderByCreatedAtAsc(chat.getId())
                        .stream().map(ChatMessageDto::from).toList())
                .orElseGet(List::of);
    }

    @Transactional(readOnly = true)
    public List<ChatDto> listChats() {
        return chatRepo.findAllByOrderByUpdatedAtDesc().stream().map(ChatDto::from).toList();
    }

    private Chat newChat(String username) {
        Chat chat = new Chat();
        chat.setUserUsername(username);
        chat.setStatus(ChatStatus.OPEN);
        Instant now = Instant.now();
        chat.setCreatedAt(now);
        chat.setUpdatedAt(now);
        return chatRepo.save(chat);
    }
}
