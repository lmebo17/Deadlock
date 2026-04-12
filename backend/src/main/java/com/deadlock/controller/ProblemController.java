package com.deadlock.controller;

import com.deadlock.dto.CreateProblemRequest;
import com.deadlock.dto.ProblemDetailResponse;
import com.deadlock.dto.ProblemResponse;
import com.deadlock.service.ProblemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Problems", description = "Problem browsing and management")
public class ProblemController {

    private final ProblemService problemService;

    public ProblemController(ProblemService problemService) {
        this.problemService = problemService;
    }

    @GetMapping("/api/problems")
    @Operation(summary = "List problems", description = "Paginated list with rating filter and search")
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
    @Operation(summary = "Get problem", description = "Full problem detail with sample test cases")
    public ProblemDetailResponse getProblemBySlug(@PathVariable String slug) {
        return problemService.getProblemBySlug(slug);
    }

    @PostMapping("/api/admin/problems")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create problem (Admin)", description = "Create a new problem")
    public ResponseEntity<ProblemResponse> createProblem(
            @Valid @RequestBody CreateProblemRequest request) {
        return ResponseEntity.ok(problemService.createProblem(request));
    }
}
