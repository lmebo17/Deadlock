package com.deadlock.dto;

import com.deadlock.model.User;

public record UserProfileResponse(
        Long id, String username, String displayName, String avatarUrl,
        int eloRating, String tierLabel, int totalMatches, int wins,
        int losses, int draws, double winRate, String joinedAt
) {
    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(
                user.getId(), user.getUsername(), user.getDisplayName(),
                user.getAvatarUrl(), user.getEloRating(), user.getTierLabel(),
                0, 0, 0, 0, 0.0, user.getCreatedAt().toString());
    }
}
