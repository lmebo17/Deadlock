package com.deadlock.security;

import com.deadlock.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService("test-secret-key-that-is-at-least-32-characters-long", 86400000L);
    }

    private User createTestUser() {
        User user = new User("test@example.com", "Test User", "https://avatar.url");
        try {
            var idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, 1L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        user.setTokenVersion(0);
        return user;
    }

    @Test
    void generateTokenReturnsNonEmptyString() {
        User user = createTestUser();
        String token = jwtService.generateToken(user);
        assertThat(token).isNotBlank();
    }

    @Test
    void validateTokenReturnsTrueForValidToken() {
        User user = createTestUser();
        String token = jwtService.generateToken(user);
        assertThat(jwtService.validateToken(token)).isTrue();
    }

    @Test
    void validateTokenReturnsFalseForGarbageToken() {
        assertThat(jwtService.validateToken("not.a.real.token")).isFalse();
    }

    @Test
    void extractUserIdReturnsCorrectId() {
        User user = createTestUser();
        String token = jwtService.generateToken(user);
        assertThat(jwtService.extractUserId(token)).isEqualTo(1L);
    }

    @Test
    void extractTokenVersionReturnsCorrectVersion() {
        User user = createTestUser();
        user.setTokenVersion(5);
        String token = jwtService.generateToken(user);
        assertThat(jwtService.extractTokenVersion(token)).isEqualTo(5);
    }

    @Test
    void expiredTokenIsInvalid() {
        JwtService shortLivedService = new JwtService(
            "test-secret-key-that-is-at-least-32-characters-long", -1000L);
        User user = createTestUser();
        String token = shortLivedService.generateToken(user);
        assertThat(jwtService.validateToken(token)).isFalse();
    }
}
