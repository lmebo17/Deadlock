package com.deadlock.controller;

import com.deadlock.dto.UserProfileResponse;
import com.deadlock.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserProfileController {

    private final UserRepository userRepository;

    public UserProfileController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/{username}")
    public ResponseEntity<UserProfileResponse> profile(@PathVariable String username) {
        return userRepository.findByUsername(username)
                .map(user -> ResponseEntity.ok(UserProfileResponse.from(user)))
                .orElse(ResponseEntity.notFound().build());
    }
}
