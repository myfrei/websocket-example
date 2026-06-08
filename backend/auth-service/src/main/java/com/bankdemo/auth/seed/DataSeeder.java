package com.bankdemo.auth.seed;

import com.bankdemo.auth.domain.Role;
import com.bankdemo.auth.domain.User;
import com.bankdemo.auth.domain.UserStatus;
import com.bankdemo.auth.repo.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        seed("man", "man123", "Менеджер Банка", "+79990000000", Role.MANAGER, UserStatus.ACTIVE);
        seed("alice", "alice123", "Алиса Иванова", "+79990000001", Role.USER, UserStatus.ACTIVE);
        seed("bob", "bob123", "Борис Петров", "+79990000002", Role.USER, UserStatus.ACTIVE);
    }

    private void seed(String username, String password, String fullName, String phone,
                      Role role, UserStatus status) {
        if (users.existsByUsername(username)) {
            return;
        }
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setFullName(fullName);
        user.setPhone(phone);
        user.setRole(role);
        user.setStatus(status);
        users.save(user);
        log.info("Seeded {} '{}'", role, username);
    }
}
