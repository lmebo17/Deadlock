package com.deadlock.sandbox;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SandboxResult {
    private final int exitCode;
    private final String stdout;
    private final String stderr;
    private final long executionTimeMs;
    private final boolean timedOut;
    private final boolean oomKilled;
}
