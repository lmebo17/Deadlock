package com.deadlock.dto;

import com.deadlock.model.User;

public record OpponentInfo(
        Long id,
        String username,
        String avatarUrl,
        int eloRating,
        String tierLabel
) {
    public static OpponentInfo from(User user) {
        return new OpponentInfo(
                user.getId(),
                user.getUsername(),
                user.getAvatarUrl(),
                user.getEloRating(),
                user.getTierLabel()
        );
    }
}
