package com.deadlock.service;

import com.deadlock.model.User;

public interface UserService {

    User findOrCreateUser(String email, String displayName, String avatarUrl,
                          String provider, String providerId);

    User setUsername(Long userId, String username);

    void incrementTokenVersion(Long userId);

    User findById(Long userId);
}
