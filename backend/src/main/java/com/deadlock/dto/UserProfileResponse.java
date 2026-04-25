package com.deadlock.dto;

import com.deadlock.model.User;

public record UserProfileResponse(
        Long id, String username, String displayName, String avatarUrl,
        int eloRating, String tierLabel, int totalMatches, int wins,
        int losses, int draws, double winRate, String joinedAt
) {
    public static UserProfileResponse from(User user, int wins, int losses, int draws) {
        int total = wins + losses + draws;
        // Win rate counts draws as half-wins (standard ELO convention)
        double winRate = total == 0 ? 0.0 : (wins + 0.5 * draws) / total;
        return new UserProfileResponse(
                user.getId(), user.getUsername(), user.getDisplayName(),
                user.getAvatarUrl(), user.getEloRating(), user.getTierLabel(),
                total, wins, losses, draws, winRate,
                user.getCreatedAt().toString());
    }
}
