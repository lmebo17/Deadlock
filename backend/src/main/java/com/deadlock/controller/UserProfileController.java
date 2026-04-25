package com.deadlock.controller;

import com.deadlock.dto.MatchResponse;
import com.deadlock.dto.UserProfileResponse;
import com.deadlock.match.MatchService;
import com.deadlock.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User profiles")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserRepository userRepository;
    private final MatchService matchService;

    @GetMapping("/{username}")
    @Operation(summary = "Get user profile", description = "Public user profile by username")
    public ResponseEntity<UserProfileResponse> profile(@PathVariable String username) {
        return userRepository.findByUsername(username)
                .map(user -> ResponseEntity.ok(UserProfileResponse.from(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{username}/matches")
    @Operation(summary = "Get user match history")
    public List<MatchResponse> matchHistory(@PathVariable String username) {
        return matchService.getMatchHistory(username);
    }
}
