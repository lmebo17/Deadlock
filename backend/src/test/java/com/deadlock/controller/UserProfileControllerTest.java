package com.deadlock.controller;

import com.deadlock.model.User;
import com.deadlock.repository.UserRepository;
import com.deadlock.security.JwtAuthFilter;
import com.deadlock.security.JwtService;
import com.deadlock.security.OAuthSuccessHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserProfileController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private OAuthSuccessHandler oAuthSuccessHandler;

    @Test
    void profileReturnsUserData() throws Exception {
        User user = new User("test@example.com", "Test User", "https://avatar.url");
        user.setUsername("testuser");
        user.setEloRating(1500);
        try {
            var f = User.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(user, 1L);
        } catch (Exception e) { throw new RuntimeException(e); }

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/users/testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.eloRating").value(1500))
                .andExpect(jsonPath("$.totalMatches").value(0));
    }

    @Test
    void profileReturns404ForUnknownUser() throws Exception {
        when(userRepository.findByUsername("nobody")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/nobody"))
                .andExpect(status().isNotFound());
    }
}
