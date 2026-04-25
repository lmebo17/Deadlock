package com.deadlock.service;

import com.deadlock.dto.RunResult;
import com.deadlock.exception.ResourceNotFoundException;
import com.deadlock.model.Language;
import com.deadlock.model.Problem;
import com.deadlock.repository.ProblemRepository;
import com.deadlock.sandbox.SandboxResult;
import com.deadlock.sandbox.SandboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Runs user code against sample test cases only. No persistence, no events.
 * Returns per-sample pass/fail with input/expected/actual visible to the user.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RunService {

    private final ProblemRepository problemRepository;
    private final StorageService storageService;
    private final WrapperCodeService wrapperCodeService;
    private final SandboxService sandboxService;

    public RunResult run(String slug, Language language, String code) {
        Problem problem = problemRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Problem", slug));

        int sampleCount = problem.getSampleCount();
        if (sampleCount <= 0) {
            return new RunResult(false, null, Collections.emptyList(), 0);
        }

        List<String> inputs = new ArrayList<>(sampleCount);
        List<String> expected = new ArrayList<>(sampleCount);
        for (int i = 1; i <= sampleCount; i++) {
            String idx = String.format("%02d", i);
            String prefix = problem.getId() + "/tests/";
            inputs.add(new String(storageService.downloadFile(prefix + idx + "-input.txt"),
                    StandardCharsets.UTF_8));
            expected.add(new String(storageService.downloadFile(prefix + idx + "-output.txt"),
                    StandardCharsets.UTF_8));
        }

        String codeToRun = code;
        if (problem.getFunctionName() != null) {
            codeToRun = wrapperCodeService.wrapCode(code, problem, language);
        }

        SandboxResult result = sandboxService.executeAll(
                codeToRun, language.name(), inputs,
                problem.getTimeLimitMs(), problem.getMemoryLimitMb());

        if (result.compileError()) {
            return new RunResult(true, result.compileErrorMessage(),
                    Collections.emptyList(), result.totalExecutionTimeMs());
        }

        List<RunResult.TestRun> tests = new ArrayList<>(sampleCount);
        for (int i = 0; i < sampleCount; i++) {
            int oneBased = i + 1;
            SandboxResult.TestCaseResult tcr = result.testResults().stream()
                    .filter(t -> t.testIndex() == oneBased)
                    .findFirst()
                    .orElse(null);

            if (tcr == null) {
                tests.add(new RunResult.TestRun(oneBased, inputs.get(i), expected.get(i),
                        "", "", false, false, true));
                continue;
            }

            boolean timedOut = tcr.timedOut();
            boolean runtimeError = !timedOut && tcr.exitCode() != 0;
            boolean passed = !timedOut && !runtimeError
                    && normalize(tcr.stdout()).equals(normalize(expected.get(i)));

            tests.add(new RunResult.TestRun(
                    oneBased, inputs.get(i), expected.get(i),
                    tcr.stdout(), tcr.stderr(),
                    passed, timedOut, runtimeError));
        }

        return new RunResult(false, null, tests, result.totalExecutionTimeMs());
    }

    private String normalize(String s) {
        return Arrays.stream(s.split("\n"))
                .map(String::stripTrailing)
                .collect(Collectors.joining("\n"))
                .stripTrailing();
    }
}
