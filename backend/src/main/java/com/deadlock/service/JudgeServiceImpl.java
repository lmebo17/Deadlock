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
import java.util.Arrays;
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
        long maxExecutionTime = 0;

        try {
            for (int i = 1; i <= problem.getTestCaseCount(); i++) {
                String idx = String.format("%02d", i);
                String prefix = problem.getId() + "/tests/";
                String input = new String(storageService.downloadFile(prefix + idx + "-input.txt"), StandardCharsets.UTF_8);
                String expectedOutput = new String(storageService.downloadFile(prefix + idx + "-output.txt"), StandardCharsets.UTF_8);

                SandboxResult result = sandboxService.execute(
                        submission.getCode(), submission.getLanguage(), input,
                        problem.getTimeLimitMs(), problem.getMemoryLimitMb());

                maxExecutionTime = Math.max(maxExecutionTime, result.getExecutionTimeMs());

                if (result.isTimedOut()) {
                    finishSubmission(submission, "TLE", i, maxExecutionTime);
                    return;
                }
                if (result.isOomKilled()) {
                    finishSubmission(submission, "MLE", i, maxExecutionTime);
                    return;
                }
                if (result.getExitCode() == 2) {
                    finishSubmission(submission, "COMPILE_ERROR", i, maxExecutionTime);
                    return;
                }
                if (result.getExitCode() != 0) {
                    finishSubmission(submission, "RUNTIME_ERROR", i, maxExecutionTime);
                    return;
                }
                if (!normalizeOutput(result.getStdout()).equals(normalizeOutput(expectedOutput))) {
                    finishSubmission(submission, "WRONG_ANSWER", i, maxExecutionTime);
                    return;
                }
            }

            finishSubmission(submission, "ACCEPTED", null, maxExecutionTime);
        } catch (Exception e) {
            log.error("Judge failed for submission {}", submission.getId(), e);
            finishSubmission(submission, "RUNTIME_ERROR", null, maxExecutionTime);
        }
    }

    private void finishSubmission(Submission submission, String verdict, Integer failedTestCase, long executionTimeMs) {
        submission.setVerdict(verdict);
        submission.setFailedTestCase(failedTestCase);
        submission.setExecutionTimeMs((int) executionTimeMs);
        submission.setStatus("COMPLETED");
        submissionRepository.save(submission);
    }

    private String normalizeOutput(String output) {
        return Arrays.stream(output.split("\n"))
                .map(String::stripTrailing)
                .collect(Collectors.joining("\n"))
                .stripTrailing();
    }
}
