package com.deadlock.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateProblemRequest(
        @NotBlank String title,
        @NotBlank String slug,
        @NotBlank String description,
        @NotBlank String inputFormat,
        @NotBlank String outputFormat,
        @NotBlank String constraints,
        @NotNull @Min(100) Integer rating,
        Integer timeLimitMs,
        Integer memoryLimitMb
) {}
