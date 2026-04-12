package com.deadlock.service;

import com.deadlock.model.Problem;
import com.deadlock.model.Submission;
import com.deadlock.model.User;
import com.deadlock.repository.SubmissionRepository;
import com.deadlock.sandbox.SandboxResult;
import com.deadlock.sandbox.SandboxService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JudgeServiceTest {

    @Mock private SandboxService sandboxService;
    @Mock private StorageService storageService;
    @Mock private SubmissionRepository submissionRepository;

    private JudgeServiceImpl judgeService;

    @BeforeEach
    void setUp() {
        judgeService = new JudgeServiceImpl(sandboxService, storageService, submissionRepository);
    }

    private Submission createSubmission(Problem problem) {
        Submission s = new Submission();
        s.setLanguage("PYTHON");
        s.setCode("print(42)");
        s.setStatus("PENDING");
        User user = new User("test@test.com", "Test", "");
        s.setUser(user);
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

    @Test
    void acceptedWhenAllTestsPass() {
        Problem problem = createProblem(2);
        Submission submission = createSubmission(problem);

        when(storageService.downloadFile("1/tests/01-input.txt")).thenReturn("5\n".getBytes());
        when(storageService.downloadFile("1/tests/01-output.txt")).thenReturn("42\n".getBytes());
        when(storageService.downloadFile("1/tests/02-input.txt")).thenReturn("10\n".getBytes());
        when(storageService.downloadFile("1/tests/02-output.txt")).thenReturn("42\n".getBytes());

        when(sandboxService.execute(anyString(), anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(new SandboxResult(0, "42\n", "", 100, false, false));
        when(submissionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        judgeService.judge(submission);

        assertThat(submission.getVerdict()).isEqualTo("ACCEPTED");
        assertThat(submission.getStatus()).isEqualTo("COMPLETED");
    }

    @Test
    void wrongAnswerWhenOutputMismatch() {
        Problem problem = createProblem(2);
        Submission submission = createSubmission(problem);

        when(storageService.downloadFile("1/tests/01-input.txt")).thenReturn("5\n".getBytes());
        when(storageService.downloadFile("1/tests/01-output.txt")).thenReturn("42\n".getBytes());

        when(sandboxService.execute(anyString(), anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(new SandboxResult(0, "wrong\n", "", 100, false, false));
        when(submissionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        judgeService.judge(submission);

        assertThat(submission.getVerdict()).isEqualTo("WRONG_ANSWER");
        assertThat(submission.getFailedTestCase()).isEqualTo(1);
    }

    @Test
    void tleWhenTimedOut() {
        Problem problem = createProblem(1);
        Submission submission = createSubmission(problem);

        when(storageService.downloadFile("1/tests/01-input.txt")).thenReturn("5\n".getBytes());
        when(storageService.downloadFile("1/tests/01-output.txt")).thenReturn("42\n".getBytes());

        when(sandboxService.execute(anyString(), anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(new SandboxResult(124, "", "", 5000, true, false));
        when(submissionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        judgeService.judge(submission);

        assertThat(submission.getVerdict()).isEqualTo("TLE");
    }

    @Test
    void mleWhenOomKilled() {
        Problem problem = createProblem(1);
        Submission submission = createSubmission(problem);

        when(storageService.downloadFile("1/tests/01-input.txt")).thenReturn("5\n".getBytes());
        when(storageService.downloadFile("1/tests/01-output.txt")).thenReturn("42\n".getBytes());

        when(sandboxService.execute(anyString(), anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(new SandboxResult(137, "", "", 1000, false, true));
        when(submissionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        judgeService.judge(submission);

        assertThat(submission.getVerdict()).isEqualTo("MLE");
    }

    @Test
    void compileErrorWhenExitCode2() {
        Problem problem = createProblem(1);
        Submission submission = createSubmission(problem);

        when(storageService.downloadFile("1/tests/01-input.txt")).thenReturn("5\n".getBytes());
        when(storageService.downloadFile("1/tests/01-output.txt")).thenReturn("42\n".getBytes());

        when(sandboxService.execute(anyString(), anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(new SandboxResult(2, "", "error: ';' expected", 500, false, false));
        when(submissionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        judgeService.judge(submission);

        assertThat(submission.getVerdict()).isEqualTo("COMPILE_ERROR");
    }

    @Test
    void runtimeErrorWhenNonZeroExit() {
        Problem problem = createProblem(1);
        Submission submission = createSubmission(problem);

        when(storageService.downloadFile("1/tests/01-input.txt")).thenReturn("5\n".getBytes());
        when(storageService.downloadFile("1/tests/01-output.txt")).thenReturn("42\n".getBytes());

        when(sandboxService.execute(anyString(), anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(new SandboxResult(1, "", "NullPointerException", 300, false, false));
        when(submissionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        judgeService.judge(submission);

        assertThat(submission.getVerdict()).isEqualTo("RUNTIME_ERROR");
    }

    @Test
    void outputComparisonTrimsWhitespace() {
        Problem problem = createProblem(1);
        Submission submission = createSubmission(problem);

        when(storageService.downloadFile("1/tests/01-input.txt")).thenReturn("5\n".getBytes());
        when(storageService.downloadFile("1/tests/01-output.txt")).thenReturn("42  \n\n".getBytes());

        when(sandboxService.execute(anyString(), anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(new SandboxResult(0, "42\n", "", 100, false, false));
        when(submissionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        judgeService.judge(submission);

        assertThat(submission.getVerdict()).isEqualTo("ACCEPTED");
    }
}
