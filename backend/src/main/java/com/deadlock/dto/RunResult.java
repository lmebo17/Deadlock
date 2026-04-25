package com.deadlock.dto;

import java.util.List;

public record RunResult(
        boolean compileError,
        String compileErrorMessage,
        List<TestRun> tests,
        long totalExecutionTimeMs
) {
    public record TestRun(
            int index,
            String input,
            String expected,
            String actual,
            String stderr,
            boolean passed,
            boolean timedOut,
            boolean runtimeError
    ) {}
}
