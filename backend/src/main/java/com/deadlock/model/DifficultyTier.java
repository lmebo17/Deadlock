package com.deadlock.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DifficultyTier {
    BEGINNER("Beginner", 0, 1000),
    EASY("Easy", 1001, 1400),
    MEDIUM("Medium", 1401, 1800),
    HARD("Hard", 1801, 2200),
    EXPERT("Expert", 2201, Integer.MAX_VALUE);

    private final String label;
    private final int minRating;
    private final int maxRating;

    public static DifficultyTier fromRating(int rating) {
        for (DifficultyTier tier : values()) {
            if (rating >= tier.minRating && rating <= tier.maxRating) {
                return tier;
            }
        }
        return BEGINNER;
    }
}
