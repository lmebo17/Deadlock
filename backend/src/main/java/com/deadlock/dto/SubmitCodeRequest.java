package com.deadlock.dto;

import jakarta.validation.constraints.NotBlank;

public record SubmitCodeRequest(@NotBlank String language, @NotBlank String code) {}
