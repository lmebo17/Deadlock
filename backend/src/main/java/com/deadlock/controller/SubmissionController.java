package com.deadlock.controller;

import com.deadlock.dto.RunResult;
import com.deadlock.dto.SubmissionResponse;
import com.deadlock.dto.SubmitCodeRequest;
import com.deadlock.model.Language;
import com.deadlock.model.User;
import com.deadlock.service.RunService;
import com.deadlock.service.SubmissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@Tag(name = "Submissions", description = "Code submission and judging")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;
    private final RunService runService;

    @PostMapping("/api/problems/{slug}/run")
    @Operation(summary = "Run code against sample test cases",
               description = "Synchronous: runs code against the visible sample tests only. No persistence.")
    public ResponseEntity<RunResult> run(
            @PathVariable String slug,
            @Valid @RequestBody SubmitCodeRequest request,
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Language lang = Language.valueOf(request.language());
        return ResponseEntity.ok(runService.run(slug, lang, request.code()));
    }

    @PostMapping("/api/problems/{slug}/submit")
    @Operation(summary = "Submit code", description = "Submit code for judging, returns submission ID")
    public ResponseEntity<?> submit(
            @PathVariable String slug,
            @Valid @RequestBody SubmitCodeRequest request,
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Long id = submissionService.submit(user.getId(), slug, request.language(), request.code());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of("id", id));
    }

    @GetMapping("/api/submissions/{id}")
    @Operation(summary = "Get submission", description = "Get submission status and verdict")
    public SubmissionResponse getSubmission(@PathVariable Long id) {
        return submissionService.getSubmission(id);
    }

    @GetMapping("/api/problems/{slug}/submissions")
    @Operation(summary = "Get my submissions", description = "Get authenticated user's submissions for a problem")
    public List<SubmissionResponse> mySubmissions(
            @PathVariable String slug,
            @AuthenticationPrincipal User user) {
        if (user == null) return List.of();
        return submissionService.getUserSubmissionsForProblem(user.getId(), slug);
    }
}
