package com.bankdemo.chat.web;

import com.bankdemo.chat.dto.ChatDto;
import com.bankdemo.chat.dto.ChatMessageDto;
import com.bankdemo.chat.dto.PostMessageRequest;
import com.bankdemo.chat.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/internal/chats")
public class InternalChatController {

    private final ChatService chatService;

    public InternalChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/{username}/open")
    public ChatDto open(@PathVariable String username) {
        return chatService.open(username);
    }

    @PostMapping("/{username}/close")
    public ChatDto close(@PathVariable String username) {
        return chatService.close(username);
    }

    @PostMapping("/{username}/messages")
    public ChatMessageDto post(@PathVariable String username, @Valid @RequestBody PostMessageRequest req) {
        return chatService.postMessage(username, req.fromUsername(), req.fromRole(), req.text());
    }

    @GetMapping("/{username}/messages")
    public List<ChatMessageDto> messages(@PathVariable String username) {
        return chatService.listMessages(username);
    }

    @GetMapping
    public List<ChatDto> chats() {
        return chatService.listChats();
    }
}
