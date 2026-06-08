package com.bankdemo.auth.repo;

import com.bankdemo.auth.domain.User;
import com.bankdemo.auth.domain.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    List<User> findByStatusOrderByCreatedAtAsc(UserStatus status);

    List<User> findAllByOrderByCreatedAtAsc();
}
