package com.deadlock.controller;

import com.deadlock.dto.ContributionDay;
import com.deadlock.dto.EloHistoryPoint;
import com.deadlock.dto.MatchResponse;
import com.deadlock.dto.UserProfileResponse;
import com.deadlock.match.MatchService;
import com.deadlock.model.Match;
import com.deadlock.repository.MatchRepository;
import com.deadlock.repository.SubmissionRepository;
import com.deadlock.repository.UserRepository;
import com.deadlock.exception.ResourceNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
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
    private final MatchRepository matchRepository;
    private final SubmissionRepository submissionRepository;
    private final MatchService matchService;

    @GetMapping("/{username}")
    @Operation(summary = "Get user profile", description = "Public user profile by username")
    public ResponseEntity<UserProfileResponse> profile(@PathVariable String username) {
        return userRepository.findByUsername(username)
                .map(user -> {
                    var stats = matchRepository.aggregateStats(user.getId());
                    int wins = stats == null || stats.getWins() == null ? 0 : stats.getWins().intValue();
                    int losses = stats == null || stats.getLosses() == null ? 0 : stats.getLosses().intValue();
                    int draws = stats == null || stats.getDraws() == null ? 0 : stats.getDraws().intValue();
                    return ResponseEntity.ok(UserProfileResponse.from(user, wins, losses, draws));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{username}/matches")
    @Operation(summary = "Get user match history")
    public List<MatchResponse> matchHistory(@PathVariable String username) {
        return matchService.getMatchHistory(username);
    }

    @GetMapping("/{username}/contributions")
    @Operation(summary = "Get user contribution heatmap data",
               description = "Daily submission counts for the past 365 days")
    @Transactional(readOnly = true)
    public List<ContributionDay> contributions(@PathVariable String username) {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", username));
        Instant since = Instant.now().minus(365, ChronoUnit.DAYS);
        return submissionRepository.countDailySubmissions(user.getId(), since).stream()
                .map(d -> new ContributionDay(d.getDay().toString(), d.getCount().intValue()))
                .toList();
    }

    @GetMapping("/{username}/elo-history")
    @Operation(summary = "Get user ELO history",
               description = "ELO rating points after each finished match, oldest first")
    @Transactional(readOnly = true)
    public List<EloHistoryPoint> eloHistory(@PathVariable String username) {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", username));
        var matches = matchRepository.findFinishedByUserIdAsc(user.getId());
        var points = new ArrayList<EloHistoryPoint>();
        for (Match m : matches) {
            boolean isPlayer1 = m.getPlayer1().getId().equals(user.getId());
            Integer change = isPlayer1 ? m.getPlayer1EloChange() : m.getPlayer2EloChange();
            int before = isPlayer1 ? m.getPlayer1EloBefore() : m.getPlayer2EloBefore();
            if (change == null) continue;
            points.add(new EloHistoryPoint(
                    m.getEndedAt() != null ? m.getEndedAt().toString() : m.getStartedAt().toString(),
                    before + change,
                    m.getId()
            ));
        }
        return points;
    }
}
