package com.deadlock.controller;

import com.deadlock.model.User;
import com.deadlock.security.JwtAuthFilter;
import com.deadlock.security.JwtService;
import com.deadlock.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    private User createAuthenticatedUser() {
        User user = new User("test@example.com", "Test User", "https://avatar.url");
        try {
            var idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, 1L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        user.setUsername("testuser");
        user.setEloRating(1200);

        var auth = new UsernamePasswordAuthenticationToken(user, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
        return user;
    }

    @Test
    void meReturnsCurrentUser() throws Exception {
        createAuthenticatedUser();

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.eloRating").value(1200));
    }

    @Test
    void meReturns401WhenNotAuthenticated() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void setUsernameSetsValidUsername() throws Exception {
        User user = createAuthenticatedUser();
        User updatedUser = new User("test@example.com", "Test User", "https://avatar.url");
        try {
            var idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(updatedUser, 1L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        updatedUser.setUsername("newname");

        when(userService.setUsername(1L, "newname")).thenReturn(updatedUser);

        mockMvc.perform(post("/api/auth/username")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("username", "newname"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newname"));
    }

    @Test
    void logoutClearsCookieAndReturnsOk() throws Exception {
        createAuthenticatedUser();

        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(cookie().maxAge("deadlock_token", 0));

        verify(userService).incrementTokenVersion(1L);
    }
}
