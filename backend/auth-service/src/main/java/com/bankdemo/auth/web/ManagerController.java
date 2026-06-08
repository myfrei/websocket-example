package com.bankdemo.auth.web;

import com.bankdemo.auth.domain.User;
import com.bankdemo.auth.domain.UserStatus;
import com.bankdemo.auth.dto.UserDto;
import com.bankdemo.auth.repo.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/manager/users")
public class ManagerController {

    private final UserRepository users;

    public ManagerController(UserRepository users) {
        this.users = users;
    }

    @GetMapping
    public List<UserDto> all() {
        return users.findAllByOrderByCreatedAtAsc().stream().map(UserDto::from).toList();
    }

    @GetMapping("/pending")
    public List<UserDto> pending() {
        return users.findByStatusOrderByCreatedAtAsc(UserStatus.PENDING).stream().map(UserDto::from).toList();
    }

    @PostMapping("/{username}/approve")
    @Transactional
    public UserDto approve(@PathVariable String username) {
        User user = users.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        user.setStatus(UserStatus.ACTIVE);
        users.save(user);
        return UserDto.from(user);
    }
}
