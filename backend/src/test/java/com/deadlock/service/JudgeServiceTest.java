package com.deadlock.service;

import com.deadlock.model.*;
import com.deadlock.repository.SubmissionRepository;
import com.deadlock.sandbox.SandboxResult;
import com.deadlock.sandbox.SandboxService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JudgeServiceTest {

    @Mock private SandboxService sandboxService;
    @Mock private StorageService storageService;
    @Mock private SubmissionRepository submissionRepository;
    @Mock private ApplicationEventPublisher eventPublisher;

    private JudgeServiceImpl judgeService;

    @BeforeEach
    void setUp() {
        judgeService = new JudgeServiceImpl(sandboxService, storageService, submissionRepository, eventPublisher);
    }

    private Submission createSubmission(Problem problem) {
        Submission s = new Submission();
        s.setLanguage(Language.PYTHON);
        s.setCode("print(42)");
        s.setUser(new User("test@test.com", "Test", ""));
        s.setProblem(problem);
        return s;
    }

    private Problem createProblem(int testCaseCount) {
        Problem p = new Problem();
        p.setTitle("Test");
        p.setSlug("test");
        p.setDescription("test");
        p.setInputFormat("test");
        p.setOutputFormat("test");
        p.setConstraints("test");
        p.setTimeLimitMs(2000);
        p.setMemoryLimitMb(256);
        p.setTestCaseCount(testCaseCount);
        p.setSampleCount(1);
        try {
            var f = Problem.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(p, 1L);
        } catch (Exception e) { throw new RuntimeException(e); }
        return p;
    }

    private void mockTestFiles(int count) {
        for (int i = 1; i <= count; i++) {
            String idx = String.format("%02d", i);
            when(storageService.downloadFile("1/tests/" + idx + "-input.txt")).thenReturn("5\n".getBytes());
            when(storageService.downloadFile("1/tests/" + idx + "-output.txt")).thenReturn("42\n".getBytes());
        }
    }

    @Test
    void acceptedWhenAllTestsPass() {
        Problem problem = createProblem(2);
        Submission submission = createSubmission(problem);
        mockTestFiles(2);

        when(sandboxService.executeAll(anyString(), anyString(), anyList(), anyInt(), anyInt()))
                .thenReturn(new SandboxResult(false, null, List.of(
                        new SandboxResult.TestCaseResult(1, 0, "42\n", "", false),
                        new SandboxResult.TestCaseResult(2, 0, "42\n", "", false)
                ), 200, false));
        when(submissionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        judgeService.judge(submission);

        assertThat(submission.getVerdict()).isEqualTo(Verdict.ACCEPTED);
        assertThat(submission.getStatus()).isEqualTo(SubmissionStatus.COMPLETED);
    }

    @Test
    void wrongAnswerWhenOutputMismatch() {
        Problem problem = createProblem(2);
        Submission submission = createSubmission(problem);
        mockTestFiles(2);

        when(sandboxService.executeAll(anyString(), anyString(), anyList(), anyInt(), anyInt()))
                .thenReturn(new SandboxResult(false, null, List.of(
                        new SandboxResult.TestCaseResult(1, 0, "wrong\n", "", false),
                        new SandboxResult.TestCaseResult(2, 0, "42\n", "", false)
                ), 200, false));
        when(submissionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        judgeService.judge(submission);

        assertThat(submission.getVerdict()).isEqualTo(Verdict.WRONG_ANSWER);
        assertThat(submission.getFailedTestCase()).isEqualTo(1);
    }

    @Test
    void tleWhenTimedOut() {
        Problem problem = createProblem(1);
        Submission submission = createSubmission(problem);
        mockTestFiles(1);

        when(sandboxService.executeAll(anyString(), anyString(), anyList(), anyInt(), anyInt()))
                .thenReturn(new SandboxResult(false, null, List.of(
                        new SandboxResult.TestCaseResult(1, 124, "", "", true)
                ), 5000, false));
        when(submissionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        judgeService.judge(submission);

        assertThat(submission.getVerdict()).isEqualTo(Verdict.TLE);
    }

    @Test
    void mleWhenOomKilled() {
        Problem problem = createProblem(1);
        Submission submission = createSubmission(problem);
        mockTestFiles(1);

        when(sandboxService.executeAll(anyString(), anyString(), anyList(), anyInt(), anyInt()))
                .thenReturn(new SandboxResult(false, null, List.of(), 1000, true));
        when(submissionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        judgeService.judge(submission);

        assertThat(submission.getVerdict()).isEqualTo(Verdict.MLE);
    }

    @Test
    void compileError() {
        Problem problem = createProblem(1);
        Submission submission = createSubmission(problem);
        mockTestFiles(1);

        when(sandboxService.executeAll(anyString(), anyString(), anyList(), anyInt(), anyInt()))
                .thenReturn(new SandboxResult(true, "error: ';' expected", List.of(), 500, false));
        when(submissionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        judgeService.judge(submission);

        assertThat(submission.getVerdict()).isEqualTo(Verdict.COMPILE_ERROR);
    }

    @Test
    void runtimeErrorWhenNonZeroExit() {
        Problem problem = createProblem(1);
        Submission submission = createSubmission(problem);
        mockTestFiles(1);

        when(sandboxService.executeAll(anyString(), anyString(), anyList(), anyInt(), anyInt()))
                .thenReturn(new SandboxResult(false, null, List.of(
                        new SandboxResult.TestCaseResult(1, 1, "", "NullPointerException", false)
                ), 300, false));
        when(submissionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        judgeService.judge(submission);

        assertThat(submission.getVerdict()).isEqualTo(Verdict.RUNTIME_ERROR);
    }

    @Test
    void outputComparisonTrimsWhitespace() {
        Problem problem = createProblem(1);
        Submission submission = createSubmission(problem);

        when(storageService.downloadFile("1/tests/01-input.txt")).thenReturn("5\n".getBytes());
        when(storageService.downloadFile("1/tests/01-output.txt")).thenReturn("42  \n\n".getBytes());

        when(sandboxService.executeAll(anyString(), anyString(), anyList(), anyInt(), anyInt()))
                .thenReturn(new SandboxResult(false, null, List.of(
                        new SandboxResult.TestCaseResult(1, 0, "42\n", "", false)
                ), 100, false));
        when(submissionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        judgeService.judge(submission);

        assertThat(submission.getVerdict().name()).isEqualTo("ACCEPTED");
    }
}
