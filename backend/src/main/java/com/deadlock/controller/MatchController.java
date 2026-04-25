package com.deadlock.controller;

import com.deadlock.dto.MatchResponse;
import com.deadlock.dto.SubmissionResponse;
import com.deadlock.match.MatchService;
import com.deadlock.model.User;
import com.deadlock.repository.SubmissionRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
@Tag(name = "Matches", description = "Match details and history")
public class MatchController {

    private final MatchService matchService;
    private final SubmissionRepository submissionRepository;

    @GetMapping("/{id}")
    @Operation(summary = "Get match details")
    public ResponseEntity<MatchResponse> getMatch(@PathVariable Long id,
                                                   @AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).build();
        MatchResponse match = matchService.getMatch(id);
        return ResponseEntity.ok(match);
    }

    @GetMapping("/active")
    @Operation(summary = "Get active match for the current user")
    public ResponseEntity<MatchResponse> getActiveMatch(@AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).build();
        Optional<MatchResponse> active = matchService.getActiveMatch(user.getId());
        return active.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/{id}/submissions")
    @Operation(summary = "Get all submissions for a match")
    @Transactional(readOnly = true)
    public ResponseEntity<List<SubmissionResponse>> getMatchSubmissions(@PathVariable Long id,
                                                                        @AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).build();
        List<SubmissionResponse> subs = submissionRepository.findByMatchIdOrderBySubmittedAtAsc(id)
                .stream().map(SubmissionResponse::from).toList();
        return ResponseEntity.ok(subs);
    }
}
