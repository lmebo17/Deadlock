package com.deadlock.controller;

import com.deadlock.dto.LeaderboardEntryResponse;
import com.deadlock.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Tag(name = "Leaderboard", description = "Player rankings")
public class LeaderboardController {

    private final UserRepository userRepository;

    public LeaderboardController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/leaderboard")
    @Operation(summary = "Get leaderboard", description = "Top players by ELO rating, paginated")
    public Page<LeaderboardEntryResponse> leaderboard(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        var users = userRepository.findByUsernameIsNotNullOrderByEloRatingDesc(
                PageRequest.of(page, size));
        int startRank = page * size + 1;
        return users.map(user -> LeaderboardEntryResponse.from(user, startRank + users.getContent().indexOf(user)));
    }
}
