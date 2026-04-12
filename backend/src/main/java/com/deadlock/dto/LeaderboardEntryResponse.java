package com.deadlock.dto;

import com.deadlock.model.User;

public record LeaderboardEntryResponse(
        int rank, Long id, String username, String avatarUrl, int eloRating, String tierLabel
) {
    public static LeaderboardEntryResponse from(User user, int rank) {
        return new LeaderboardEntryResponse(
                rank, user.getId(), user.getUsername(),
                user.getAvatarUrl(), user.getEloRating(), user.getTierLabel());
    }
}
