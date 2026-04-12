package com.deadlock.controller;

import com.deadlock.dto.SetUsernameRequest;
import com.deadlock.dto.UserResponse;
import com.deadlock.model.User;
import com.deadlock.security.JwtAuthFilter;
import com.deadlock.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(UserResponse.from(user));
    }

    @PostMapping("/username")
    public ResponseEntity<UserResponse> setUsername(@AuthenticationPrincipal User user,
                                                     @Valid @RequestBody SetUsernameRequest request) {
        User updated = userService.setUsername(user.getId(), request.username());
        return ResponseEntity.ok(UserResponse.from(updated));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal User user,
                                        HttpServletResponse response) {
        userService.incrementTokenVersion(user.getId());

        Cookie cookie = new Cookie(JwtAuthFilter.COOKIE_NAME, "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        return ResponseEntity.ok().build();
    }
}
