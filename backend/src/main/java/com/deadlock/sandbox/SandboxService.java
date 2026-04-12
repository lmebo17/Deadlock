package com.deadlock.sandbox;

public interface SandboxService {
    SandboxResult execute(String code, String language, String input,
                          int timeLimitMs, int memoryLimitMb);
}
