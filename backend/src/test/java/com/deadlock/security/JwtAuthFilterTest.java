package com.deadlock.security;

import com.deadlock.model.User;
import com.deadlock.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FilterChain filterChain;

    private JwtAuthFilter jwtAuthFilter;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService("test-secret-key-that-is-at-least-32-characters-long", 86400000L);
        jwtAuthFilter = new JwtAuthFilter(jwtService, userRepository);
        SecurityContextHolder.clearContext();
    }

    private User createTestUser(Long id, int tokenVersion) {
        User user = new User("test@example.com", "Test User", "https://avatar.url");
        try {
            var idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        user.setTokenVersion(tokenVersion);
        return user;
    }

    @Test
    void continuesFilterChainWithoutCookie() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void setsAuthenticationWithValidToken() throws Exception {
        User user = createTestUser(1L, 0);
        String token = jwtService.generateToken(user);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("deadlock_token", token));
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo(user);
    }

    @Test
    void rejectsTokenWithWrongTokenVersion() throws Exception {
        User user = createTestUser(1L, 0);
        String token = jwtService.generateToken(user);

        User updatedUser = createTestUser(1L, 1);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("deadlock_token", token));
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(userRepository.findById(1L)).thenReturn(Optional.of(updatedUser));

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void rejectsInvalidToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("deadlock_token", "garbage-token"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
