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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LeaderboardController.class)
@AutoConfigureMockMvc(addFilters = false)
class LeaderboardControllerTest {

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
    void leaderboardReturnsPaginatedUsers() throws Exception {
        User user = new User("test@example.com", "Top Player", "https://avatar.url");
        user.setUsername("topplayer");
        user.setEloRating(1800);
        try {
            var f = User.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(user, 1L);
        } catch (Exception e) { throw new RuntimeException(e); }

        when(userRepository.findByUsernameIsNotNullOrderByEloRatingDesc(any()))
                .thenReturn(new PageImpl<>(List.of(user), PageRequest.of(0, 50), 1));

        mockMvc.perform(get("/api/leaderboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].username").value("topplayer"))
                .andExpect(jsonPath("$.content[0].eloRating").value(1800))
                .andExpect(jsonPath("$.content[0].rank").value(1));
    }
}
