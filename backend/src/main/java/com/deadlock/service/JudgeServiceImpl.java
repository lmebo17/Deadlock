package com.deadlock.service;

import com.deadlock.event.SubmissionJudgedEvent;
import com.deadlock.model.Problem;
import com.deadlock.model.Submission;
import com.deadlock.model.SubmissionStatus;
import com.deadlock.model.Verdict;
import com.deadlock.repository.SubmissionRepository;
import com.deadlock.sandbox.SandboxResult;
import com.deadlock.sandbox.SandboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class JudgeServiceImpl implements JudgeService {

    private final SandboxService sandboxService;
    private final StorageService storageService;
    private final SubmissionRepository submissionRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final WrapperCodeService wrapperCodeService;

    @Override
    @Async("judgeExecutor")
    public void judge(Submission submission) {
        submission.setStatus(SubmissionStatus.JUDGING);
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

            // Wrap code if problem uses function signatures
            String codeToRun = submission.getCode();
            if (problem.getFunctionName() != null) {
                codeToRun = wrapperCodeService.wrapCode(
                        submission.getCode(), problem, submission.getLanguage());
            }

            // Execute all tests in one container
            SandboxResult result = sandboxService.executeAll(
                    codeToRun, submission.getLanguage().name(), testInputs,
                    problem.getTimeLimitMs(), problem.getMemoryLimitMb());

            // Handle compile error
            if (result.compileError()) {
                finishSubmission(submission, Verdict.COMPILE_ERROR, 1, result.totalExecutionTimeMs());
                return;
            }

            // Handle OOM
            if (result.oomKilled()) {
                finishSubmission(submission, Verdict.MLE, null, result.totalExecutionTimeMs());
                return;
            }

            // Check each test result
            for (SandboxResult.TestCaseResult tcResult : result.testResults()) {
                int idx = tcResult.testIndex();

                if (tcResult.timedOut()) {
                    finishSubmission(submission, Verdict.TLE, idx, result.totalExecutionTimeMs());
                    return;
                }
                if (tcResult.exitCode() != 0) {
                    finishSubmission(submission, Verdict.RUNTIME_ERROR, idx, result.totalExecutionTimeMs());
                    return;
                }
                if (!normalizeOutput(tcResult.stdout()).equals(
                        normalizeOutput(expectedOutputs.get(idx - 1)))) {
                    finishSubmission(submission, Verdict.WRONG_ANSWER, idx, result.totalExecutionTimeMs());
                    return;
                }
            }

            // If we got fewer results than test cases, something went wrong
            if (result.testResults().size() < problem.getTestCaseCount()) {
                finishSubmission(submission, Verdict.RUNTIME_ERROR, result.testResults().size() + 1,
                        result.totalExecutionTimeMs());
                return;
            }

            finishSubmission(submission, Verdict.ACCEPTED, null, result.totalExecutionTimeMs());
        } catch (RuntimeException e) {
            log.error("Judge failed for submission {}", submission.getId(), e);
            finishSubmission(submission, Verdict.RUNTIME_ERROR, null, 0);
        }
    }

    private void finishSubmission(Submission submission, Verdict verdict, Integer failedTestCase, long executionTimeMs) {
        submission.setVerdict(verdict);
        submission.setFailedTestCase(failedTestCase);
        submission.setExecutionTimeMs((int) executionTimeMs);
        submission.setStatus(SubmissionStatus.COMPLETED);
        submissionRepository.save(submission);
        log.info("Submission {} verdict: {} ({}ms)", submission.getId(), verdict, executionTimeMs);

        eventPublisher.publishEvent(new SubmissionJudgedEvent(this, submission.getId(), verdict.name()));
    }

    private String normalizeOutput(String output) {
        return Arrays.stream(output.split("\n"))
                .map(String::stripTrailing)
                .collect(Collectors.joining("\n"))
                .stripTrailing();
    }
}
