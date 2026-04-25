package com.deadlock.dto;

import com.deadlock.model.DifficultyPreference;
import com.deadlock.model.TimeControl;
import jakarta.validation.constraints.NotNull;

public record QueueJoinRequest(
        @NotNull TimeControl timeControl,
        @NotNull DifficultyPreference difficulty
) {}
