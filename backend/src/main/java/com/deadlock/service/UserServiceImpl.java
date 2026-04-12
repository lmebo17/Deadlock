package com.deadlock.service;

import com.deadlock.exception.InvalidInputException;
import com.deadlock.exception.ResourceNotFoundException;
import com.deadlock.exception.UsernameAlreadyTakenException;
import com.deadlock.model.User;
import com.deadlock.model.UserProvider;
import com.deadlock.repository.UserProviderRepository;
import com.deadlock.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

@Service
public class UserServiceImpl implements UserService {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");

    private final UserRepository userRepository;
    private final UserProviderRepository userProviderRepository;

    public UserServiceImpl(UserRepository userRepository, UserProviderRepository userProviderRepository) {
        this.userRepository = userRepository;
        this.userProviderRepository = userProviderRepository;
    }

    @Override
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

    @Override
    @Transactional
    public User setUsername(Long userId, String username) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));

        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new InvalidInputException(
                    "Username must be 3-20 characters, alphanumeric and underscores only");
        }

        if (userRepository.existsByUsername(username)) {
            throw new UsernameAlreadyTakenException(username);
        }

        user.setUsername(username);
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void incrementTokenVersion(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));
        user.setTokenVersion(user.getTokenVersion() + 1);
        userRepository.save(user);
    }

    @Override
    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));
    }
}
