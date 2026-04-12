package com.deadlock.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Tier {
    NEWBIE("Newbie", 0, 1199),
    PUPIL("Pupil", 1200, 1399),
    SPECIALIST("Specialist", 1400, 1599),
    EXPERT("Expert", 1600, 1899),
    CANDIDATE_MASTER("Candidate Master", 1900, 2099),
    MASTER("Master", 2100, 2399),
    GRANDMASTER("Grandmaster", 2400, Integer.MAX_VALUE);

    private final String label;
    private final int minRating;
    private final int maxRating;

    public static Tier fromRating(int rating) {
        for (Tier tier : values()) {
            if (rating >= tier.minRating && rating <= tier.maxRating) {
                return tier;
            }
        }
        return NEWBIE;
    }
}
