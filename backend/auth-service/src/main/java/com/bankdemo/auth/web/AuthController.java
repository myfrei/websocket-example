package com.bankdemo.auth.web;

import com.bankdemo.auth.domain.Role;
import com.bankdemo.auth.domain.User;
import com.bankdemo.auth.domain.UserStatus;
import com.bankdemo.auth.dto.AuthResponse;
import com.bankdemo.auth.dto.LoginRequest;
import com.bankdemo.auth.dto.RegisterRequest;
import com.bankdemo.auth.dto.RegisterResponse;
import com.bankdemo.auth.repo.UserRepository;
import com.bankdemo.auth.security.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(UserRepository users, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public RegisterResponse register(@Valid @RequestBody RegisterRequest req) {
        if (users.existsByUsername(req.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already taken");
        }
        User user = new User();
        user.setUsername(req.username());
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        user.setFullName(req.fullName());
        user.setPhone(req.phone());
        user.setRole(Role.USER);
        user.setStatus(UserStatus.PENDING);
        users.save(user);
        return new RegisterResponse(user.getUsername(), user.getStatus().name(),
                "Регистрация принята. Ожидайте активации менеджером.");
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        User user = users.findByUsername(req.username())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Ожидайте активации менеджером");
        }
        return new AuthResponse(jwtService.issue(user), user.getUsername(),
                user.getFullName(), user.getPhone(), user.getRole().name());
    }
}
