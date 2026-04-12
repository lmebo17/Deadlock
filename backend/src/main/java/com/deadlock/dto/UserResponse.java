package com.deadlock.dto;

import com.deadlock.model.User;

public record UserResponse(
        Long id,
        String email,
        String username,
        String displayName,
        String avatarUrl,
        int eloRating
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getDisplayName(),
                user.getAvatarUrl(),
                user.getEloRating()
        );
    }
}
