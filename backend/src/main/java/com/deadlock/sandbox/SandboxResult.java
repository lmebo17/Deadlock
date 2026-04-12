package com.deadlock.sandbox;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class SandboxResult {
    private final boolean compileError;
    private final String compileErrorMessage;
    private final List<TestCaseResult> testResults;
    private final long totalExecutionTimeMs;
    private final boolean oomKilled;

    @Getter
    @AllArgsConstructor
    public static class TestCaseResult {
        private final int testIndex;
        private final int exitCode;
        private final String stdout;
        private final String stderr;
        private final boolean timedOut;
    }
}
