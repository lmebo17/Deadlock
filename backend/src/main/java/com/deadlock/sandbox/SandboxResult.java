package com.deadlock.sandbox;

import java.util.List;

public record SandboxResult(
        boolean compileError,
        String compileErrorMessage,
        List<TestCaseResult> testResults,
        long totalExecutionTimeMs,
        boolean oomKilled
) {
    public record TestCaseResult(
            int testIndex,
            int exitCode,
            String stdout,
            String stderr,
            boolean timedOut
    ) {}
}
