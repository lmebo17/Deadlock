package com.deadlock.controller;

import com.deadlock.dto.SetUsernameRequest;
import com.deadlock.dto.UserResponse;
import com.deadlock.model.User;
import com.deadlock.security.JwtAuthFilter;
import com.deadlock.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Authentication endpoints")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Returns the authenticated user's profile")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(UserResponse.from(user));
    }

    @PostMapping("/username")
    @Operation(summary = "Set username", description = "Set username on first login")
    public ResponseEntity<UserResponse> setUsername(@AuthenticationPrincipal User user,
                                                     @Valid @RequestBody SetUsernameRequest request) {
        User updated = userService.setUsername(user.getId(), request.username());
        return ResponseEntity.ok(UserResponse.from(updated));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Invalidate all sessions and clear auth cookie")
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
