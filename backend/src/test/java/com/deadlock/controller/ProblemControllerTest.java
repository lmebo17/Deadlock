package com.deadlock.controller;

import com.deadlock.dto.*;
import com.deadlock.repository.ProblemRepository;
import com.deadlock.security.JwtAuthFilter;
import com.deadlock.security.JwtService;
import com.deadlock.security.OAuthSuccessHandler;
import com.deadlock.service.ProblemService;
import com.deadlock.service.StarterCodeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProblemController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProblemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProblemService problemService;

    @MockBean
    private ProblemRepository problemRepository;

    @MockBean
    private StarterCodeService starterCodeService;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private OAuthSuccessHandler oAuthSuccessHandler;

    @Test
    void listProblemsReturnsPage() throws Exception {
        ProblemResponse pr = new ProblemResponse(1L, "Two Sum", "two-sum", 800, "Beginner",
                2000, 256, 30, 3);
        when(problemService.listProblems(anyInt(), anyInt(), anyString(), any()))
                .thenReturn(new PageImpl<>(List.of(pr), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/problems")
                        .param("minRating", "0")
                        .param("maxRating", "4000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Two Sum"))
                .andExpect(jsonPath("$.content[0].tierLabel").value("Beginner"));
    }

    @Test
    void getProblemBySlugReturnsDetail() throws Exception {
        List<TestCaseResponse> samples = List.of(
                new TestCaseResponse(1, "2 9\n2 7 11 15", "0 1"));
        ProblemDetailResponse detail = new ProblemDetailResponse(
                1L, "Two Sum", "two-sum", "Find two numbers...",
                "First line: n", "Two indices", "2 <= n", 800, "Beginner",
                2000, 256, 30, 3, samples, null, null);
        when(problemService.getProblemBySlug("two-sum")).thenReturn(detail);

        mockMvc.perform(get("/api/problems/two-sum"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Two Sum"))
                .andExpect(jsonPath("$.sampleTestCases[0].input").value("2 9\n2 7 11 15"));
    }

    @Test
    void createProblemReturnsCreatedProblem() throws Exception {
        CreateProblemRequest req = new CreateProblemRequest(
                "Two Sum", "two-sum", "Find two numbers...",
                "First line: n", "Two indices", "2 <= n", 800, 2000, 256);

        ProblemResponse response = new ProblemResponse(1L, "Two Sum", "two-sum", 800, "Beginner",
                2000, 256, 0, 0);
        when(problemService.createProblem(any())).thenReturn(response);

        mockMvc.perform(post("/api/admin/problems")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Two Sum"));
    }
}
