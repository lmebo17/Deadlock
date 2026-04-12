package com.deadlock.controller;

import com.deadlock.dto.SubmissionResponse;
import com.deadlock.model.User;
import com.deadlock.security.JwtAuthFilter;
import com.deadlock.security.JwtService;
import com.deadlock.security.OAuthSuccessHandler;
import com.deadlock.service.SubmissionService;
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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SubmissionController.class)
@AutoConfigureMockMvc(addFilters = false)
class SubmissionControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private SubmissionService submissionService;
    @MockBean private JwtAuthFilter jwtAuthFilter;
    @MockBean private JwtService jwtService;
    @MockBean private OAuthSuccessHandler oAuthSuccessHandler;

    private User authenticateUser() {
        User user = new User("test@test.com", "Test", "");
        user.setUsername("testuser");
        try {
            var f = User.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(user, 1L);
        } catch (Exception e) { throw new RuntimeException(e); }
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, List.of()));
        return user;
    }

    @Test
    void submitReturns202WithId() throws Exception {
        authenticateUser();
        when(submissionService.submit(1L, "two-sum", "PYTHON", "print(42)")).thenReturn(99L);

        mockMvc.perform(post("/api/problems/two-sum/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("language", "PYTHON", "code", "print(42)"))))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id").value(99));
    }

    @Test
    void getSubmissionReturnsStatus() throws Exception {
        authenticateUser();
        var response = new SubmissionResponse(99L, "two-sum", "PYTHON", "COMPLETED", "ACCEPTED",
                null, 150, "2026-04-12T10:00:00Z");
        when(submissionService.getSubmission(99L)).thenReturn(response);

        mockMvc.perform(get("/api/submissions/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verdict").value("ACCEPTED"))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }
}
