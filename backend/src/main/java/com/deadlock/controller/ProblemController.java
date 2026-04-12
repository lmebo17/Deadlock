package com.deadlock.controller;

import com.deadlock.dto.CreateProblemRequest;
import com.deadlock.dto.ProblemDetailResponse;
import com.deadlock.dto.ProblemResponse;
import com.deadlock.service.ProblemService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
public class ProblemController {

    private final ProblemService problemService;

    public ProblemController(ProblemService problemService) {
        this.problemService = problemService;
    }

    @GetMapping("/api/problems")
    public Page<ProblemResponse> listProblems(
            @RequestParam(defaultValue = "0") int minRating,
            @RequestParam(defaultValue = "4000") int maxRating,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return problemService.listProblems(minRating, maxRating, search,
                PageRequest.of(page, size, Sort.by("rating").ascending()));
    }

    @GetMapping("/api/problems/{slug}")
    public ProblemDetailResponse getProblemBySlug(@PathVariable String slug) {
        return problemService.getProblemBySlug(slug);
    }

    @PostMapping("/api/admin/problems")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProblemResponse> createProblem(
            @Valid @RequestBody CreateProblemRequest request) {
        return ResponseEntity.ok(problemService.createProblem(request));
    }
}
