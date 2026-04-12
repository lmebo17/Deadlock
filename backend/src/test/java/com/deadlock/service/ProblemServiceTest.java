package com.deadlock.service;

import com.deadlock.dto.CreateProblemRequest;
import com.deadlock.dto.ProblemDetailResponse;
import com.deadlock.dto.ProblemResponse;
import com.deadlock.exception.ResourceNotFoundException;
import com.deadlock.model.Problem;
import com.deadlock.repository.ProblemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProblemServiceTest {

    @Mock
    private ProblemRepository problemRepository;

    @Mock
    private StorageService storageService;

    private ProblemServiceImpl problemService;

    @BeforeEach
    void setUp() {
        problemService = new ProblemServiceImpl(problemRepository, storageService);
    }

    private Problem createTestProblem() {
        Problem p = new Problem();
        p.setTitle("Two Sum");
        p.setSlug("two-sum");
        p.setDescription("Find two numbers...");
        p.setInputFormat("First line: n and target");
        p.setOutputFormat("Two indices");
        p.setConstraints("2 <= n <= 10^5");
        p.setRating(800);
        p.setTimeLimitMs(2000);
        p.setMemoryLimitMb(256);
        p.setTestCaseCount(30);
        p.setSampleCount(3);
        try {
            var idField = Problem.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(p, 1L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return p;
    }

    @Test
    void listProblemsReturnsPaginatedResults() {
        Problem p = createTestProblem();
        Page<Problem> page = new PageImpl<>(List.of(p));
        when(problemRepository.searchByRatingAndTitle(eq(0), eq(4000), eq(""), any()))
                .thenReturn(page);

        Page<ProblemResponse> result = problemService.listProblems(0, 4000, "", PageRequest.of(0, 20));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).title()).isEqualTo("Two Sum");
        assertThat(result.getContent().get(0).tierLabel()).isEqualTo("Beginner");
    }

    @Test
    void getProblemBySlugReturnsProblemWithSamples() {
        Problem p = createTestProblem();
        when(problemRepository.findBySlug("two-sum")).thenReturn(Optional.of(p));
        when(storageService.downloadFile("1/tests/01-input.txt"))
                .thenReturn("2 9\n2 7 11 15".getBytes());
        when(storageService.downloadFile("1/tests/01-output.txt"))
                .thenReturn("0 1".getBytes());
        when(storageService.downloadFile("1/tests/02-input.txt"))
                .thenReturn("3 6\n3 2 4".getBytes());
        when(storageService.downloadFile("1/tests/02-output.txt"))
                .thenReturn("1 2".getBytes());
        when(storageService.downloadFile("1/tests/03-input.txt"))
                .thenReturn("2 6\n3 3".getBytes());
        when(storageService.downloadFile("1/tests/03-output.txt"))
                .thenReturn("0 1".getBytes());

        ProblemDetailResponse result = problemService.getProblemBySlug("two-sum");

        assertThat(result.title()).isEqualTo("Two Sum");
        assertThat(result.sampleTestCases()).hasSize(3);
        assertThat(result.sampleTestCases().get(0).input()).isEqualTo("2 9\n2 7 11 15");
    }

    @Test
    void getProblemBySlugThrowsWhenNotFound() {
        when(problemRepository.findBySlug("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> problemService.getProblemBySlug("nonexistent"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createProblemSavesAndReturns() {
        CreateProblemRequest req = new CreateProblemRequest(
                "Two Sum", "two-sum", "Find two numbers...",
                "First line: n", "Two indices", "2 <= n", 800, 2000, 256);

        when(problemRepository.save(any(Problem.class)))
                .thenAnswer(inv -> {
                    Problem p = inv.getArgument(0);
                    try {
                        var idField = Problem.class.getDeclaredField("id");
                        idField.setAccessible(true);
                        idField.set(p, 1L);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    return p;
                });

        ProblemResponse result = problemService.createProblem(req);

        assertThat(result.title()).isEqualTo("Two Sum");
        assertThat(result.slug()).isEqualTo("two-sum");
        verify(problemRepository).save(any(Problem.class));
    }
}
