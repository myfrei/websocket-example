package com.bankdemo.auth.web;

import com.bankdemo.auth.dto.UserDto;
import com.bankdemo.auth.repo.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/internal/users")
public class InternalController {

    private final UserRepository users;

    public InternalController(UserRepository users) {
        this.users = users;
    }

    @GetMapping("/{username}")
    public UserDto get(@PathVariable String username) {
        return users.findByUsername(username)
                .map(UserDto::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
