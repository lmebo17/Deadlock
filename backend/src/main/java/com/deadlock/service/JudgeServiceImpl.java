package com.deadlock.service;

import com.deadlock.model.Problem;
import com.deadlock.model.Submission;
import com.deadlock.repository.SubmissionRepository;
import com.deadlock.sandbox.SandboxResult;
import com.deadlock.sandbox.SandboxService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class JudgeServiceImpl implements JudgeService {

    private final SandboxService sandboxService;
    private final StorageService storageService;
    private final SubmissionRepository submissionRepository;

    public JudgeServiceImpl(SandboxService sandboxService, StorageService storageService,
                            SubmissionRepository submissionRepository) {
        this.sandboxService = sandboxService;
        this.storageService = storageService;
        this.submissionRepository = submissionRepository;
    }

    @Override
    @Async("judgeExecutor")
    public void judge(Submission submission) {
        submission.setStatus("JUDGING");
        submissionRepository.save(submission);

        Problem problem = submission.getProblem();

        try {
            // Fetch all test inputs and expected outputs from S3
            List<String> testInputs = new ArrayList<>();
            List<String> expectedOutputs = new ArrayList<>();

            for (int i = 1; i <= problem.getTestCaseCount(); i++) {
                String idx = String.format("%02d", i);
                String prefix = problem.getId() + "/tests/";
                testInputs.add(new String(
                        storageService.downloadFile(prefix + idx + "-input.txt"), StandardCharsets.UTF_8));
                expectedOutputs.add(new String(
                        storageService.downloadFile(prefix + idx + "-output.txt"), StandardCharsets.UTF_8));
            }

            // Execute all tests in one container
            SandboxResult result = sandboxService.executeAll(
                    submission.getCode(), submission.getLanguage(), testInputs,
                    problem.getTimeLimitMs(), problem.getMemoryLimitMb());

            // Handle compile error
            if (result.isCompileError()) {
                finishSubmission(submission, "COMPILE_ERROR", 1, result.getTotalExecutionTimeMs());
                return;
            }

            // Handle OOM
            if (result.isOomKilled()) {
                finishSubmission(submission, "MLE", null, result.getTotalExecutionTimeMs());
                return;
            }

            // Check each test result
            for (SandboxResult.TestCaseResult tcResult : result.getTestResults()) {
                int idx = tcResult.getTestIndex();

                if (tcResult.isTimedOut()) {
                    finishSubmission(submission, "TLE", idx, result.getTotalExecutionTimeMs());
                    return;
                }
                if (tcResult.getExitCode() != 0) {
                    finishSubmission(submission, "RUNTIME_ERROR", idx, result.getTotalExecutionTimeMs());
                    return;
                }
                if (!normalizeOutput(tcResult.getStdout()).equals(
                        normalizeOutput(expectedOutputs.get(idx - 1)))) {
                    finishSubmission(submission, "WRONG_ANSWER", idx, result.getTotalExecutionTimeMs());
                    return;
                }
            }

            // If we got fewer results than test cases, something went wrong
            if (result.getTestResults().size() < problem.getTestCaseCount()) {
                finishSubmission(submission, "RUNTIME_ERROR", result.getTestResults().size() + 1,
                        result.getTotalExecutionTimeMs());
                return;
            }

            finishSubmission(submission, "ACCEPTED", null, result.getTotalExecutionTimeMs());
        } catch (Exception e) {
            log.error("Judge failed for submission {}", submission.getId(), e);
            finishSubmission(submission, "RUNTIME_ERROR", null, 0);
        }
    }

    private void finishSubmission(Submission submission, String verdict, Integer failedTestCase, long executionTimeMs) {
        submission.setVerdict(verdict);
        submission.setFailedTestCase(failedTestCase);
        submission.setExecutionTimeMs((int) executionTimeMs);
        submission.setStatus("COMPLETED");
        submissionRepository.save(submission);
        log.info("Submission {} verdict: {} ({}ms)", submission.getId(), verdict, executionTimeMs);
    }

    private String normalizeOutput(String output) {
        return Arrays.stream(output.split("\n"))
                .map(String::stripTrailing)
                .collect(Collectors.joining("\n"))
                .stripTrailing();
    }
}
