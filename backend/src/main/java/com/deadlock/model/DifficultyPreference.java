package com.deadlock.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DifficultyPreference {
    BEGINNER(800, 1000),
    EASY(1001, 1400),
    MEDIUM(1401, 1800),
    HARD(1801, 2200),
    EXPERT(2201, Integer.MAX_VALUE),
    ANY(0, Integer.MAX_VALUE);

    private final int minRating;
    private final int maxRating;

    public boolean compatibleWith(DifficultyPreference other) {
        return this == ANY || other == ANY || this == other;
    }
}
