package com.deadlock.sandbox;

import java.util.List;

public interface SandboxService {

    /**
     * Execute code against all test inputs in a single container.
     * @param code source code
     * @param language JAVA, PYTHON, or CPP
     * @param testInputs list of test case inputs (index 0 = test 1)
     * @param timeLimitMs per-test time limit
     * @param memoryLimitMb container memory limit
     */
    SandboxResult executeAll(String code, String language, List<String> testInputs,
                              int timeLimitMs, int memoryLimitMb);
}
