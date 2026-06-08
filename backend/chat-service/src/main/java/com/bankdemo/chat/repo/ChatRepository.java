package com.bankdemo.chat.repo;

import com.bankdemo.chat.domain.Chat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatRepository extends JpaRepository<Chat, UUID> {

    Optional<Chat> findByUserUsername(String userUsername);

    List<Chat> findAllByOrderByUpdatedAtDesc();
}
