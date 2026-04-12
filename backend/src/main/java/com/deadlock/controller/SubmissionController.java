package com.deadlock.controller;

import com.deadlock.dto.SubmissionResponse;
import com.deadlock.dto.SubmitCodeRequest;
import com.deadlock.model.User;
import com.deadlock.service.SubmissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@Tag(name = "Submissions", description = "Code submission and judging")
public class SubmissionController {

    private final SubmissionService submissionService;

    public SubmissionController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @PostMapping("/api/problems/{slug}/submit")
    @Operation(summary = "Submit code", description = "Submit code for judging, returns submission ID")
    public ResponseEntity<Map<String, Long>> submit(
            @PathVariable String slug,
            @Valid @RequestBody SubmitCodeRequest request,
            @AuthenticationPrincipal User user) {
        Long id = submissionService.submit(user.getId(), slug, request.language(), request.code());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of("id", id));
    }

    @GetMapping("/api/submissions/{id}")
    @Operation(summary = "Get submission", description = "Get submission status and verdict")
    public SubmissionResponse getSubmission(@PathVariable Long id) {
        return submissionService.getSubmission(id);
    }
}
