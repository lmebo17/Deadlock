package com.deadlock.service;

import com.deadlock.exception.InvalidInputException;
import com.deadlock.exception.UsernameAlreadyTakenException;
import com.deadlock.model.User;
import com.deadlock.model.UserProvider;
import com.deadlock.repository.UserProviderRepository;
import com.deadlock.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserProviderRepository userProviderRepository;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository, userProviderRepository);
    }

    @Test
    void findOrCreateUser_createsNewUserWhenNotFound() {
        when(userProviderRepository.findByProviderAndProviderId("github", "12345"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.empty());
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.findOrCreateUser(
                "test@example.com", "Test User", "https://avatar.url", "github", "12345");

        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getDisplayName()).isEqualTo("Test User");
        assertThat(result.getUsername()).isNull();
        verify(userRepository, atLeastOnce()).save(any(User.class));
    }

    @Test
    void findOrCreateUser_linksProviderToExistingUserByEmail() {
        User existingUser = new User("test@example.com", "Test User", "https://avatar.url");
        when(userProviderRepository.findByProviderAndProviderId("google", "67890"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.findOrCreateUser(
                "test@example.com", "Test User", "https://avatar.url", "google", "67890");

        assertThat(result).isSameAs(existingUser);
        assertThat(result.getProviders()).hasSize(1);
        assertThat(result.getProviders().get(0).getProvider()).isEqualTo("google");
    }

    @Test
    void findOrCreateUser_returnsExistingUserWhenProviderAlreadyLinked() {
        User existingUser = new User("test@example.com", "Test User", "https://avatar.url");
        UserProvider existingProvider = new UserProvider("github", "12345");
        existingProvider.setUser(existingUser);

        when(userProviderRepository.findByProviderAndProviderId("github", "12345"))
                .thenReturn(Optional.of(existingProvider));
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.findOrCreateUser(
                "test@example.com", "Test User", "https://new-avatar.url", "github", "12345");

        assertThat(result).isSameAs(existingUser);
        assertThat(result.getAvatarUrl()).isEqualTo("https://new-avatar.url");
    }

    @Test
    void setUsername_setsUsernameWhenValid() {
        User user = new User("test@example.com", "Test User", "https://avatar.url");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername("validname")).thenReturn(false);
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.setUsername(1L, "validname");

        assertThat(result.getUsername()).isEqualTo("validname");
    }

    @Test
    void setUsername_throwsWhenUsernameTaken() {
        User user = new User("test@example.com", "Test User", "https://avatar.url");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername("taken")).thenReturn(true);

        assertThatThrownBy(() -> userService.setUsername(1L, "taken"))
                .isInstanceOf(UsernameAlreadyTakenException.class)
                .hasMessageContaining("taken");
    }

    @Test
    void setUsername_throwsWhenInvalidFormat() {
        User user = new User("test@example.com", "Test User", "https://avatar.url");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.setUsername(1L, "ab"))
                .isInstanceOf(com.deadlock.exception.InvalidInputException.class);
    }

    @Test
    void incrementTokenVersion_incrementsByOne() {
        User user = new User("test@example.com", "Test User", "https://avatar.url");
        user.setTokenVersion(3);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        userService.incrementTokenVersion(1L);

        assertThat(user.getTokenVersion()).isEqualTo(4);
        verify(userRepository).save(user);
    }
}
