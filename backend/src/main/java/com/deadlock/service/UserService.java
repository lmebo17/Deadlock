package com.deadlock.service;

import com.deadlock.model.User;
import com.deadlock.model.UserProvider;
import com.deadlock.repository.UserProviderRepository;
import com.deadlock.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

@Service
public class UserService {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");

    private final UserRepository userRepository;
    private final UserProviderRepository userProviderRepository;

    public UserService(UserRepository userRepository, UserProviderRepository userProviderRepository) {
        this.userRepository = userRepository;
        this.userProviderRepository = userProviderRepository;
    }

    @Transactional
    public User findOrCreateUser(String email, String displayName, String avatarUrl,
                                  String provider, String providerId) {
        var existingProvider = userProviderRepository.findByProviderAndProviderId(provider, providerId);
        if (existingProvider.isPresent()) {
            User user = existingProvider.get().getUser();
            user.setAvatarUrl(avatarUrl);
            user.setDisplayName(displayName);
            return userRepository.save(user);
        }

        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User(email, displayName, avatarUrl);
            return userRepository.save(newUser);
        });

        UserProvider userProvider = new UserProvider(provider, providerId);
        user.addProvider(userProvider);
        return userRepository.save(user);
    }

    @Transactional
    public User setUsername(Long userId, String username) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new IllegalArgumentException(
                    "Username must be 3-20 characters, alphanumeric and underscores only");
        }

        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username '" + username + "' is already taken");
        }

        user.setUsername(username);
        return userRepository.save(user);
    }

    @Transactional
    public void incrementTokenVersion(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setTokenVersion(user.getTokenVersion() + 1);
        userRepository.save(user);
    }

    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
