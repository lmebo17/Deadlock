package com.deadlock.service;

import com.deadlock.model.Language;
import com.deadlock.model.Problem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StarterCodeServiceTest {

    private StarterCodeServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new StarterCodeServiceImpl();
    }

    private Problem createProblem(String functionName, String returnType, String params) {
        Problem p = new Problem();
        p.setTitle("Test");
        p.setSlug("test");
        p.setDescription("test");
        p.setInputFormat("test");
        p.setOutputFormat("test");
        p.setConstraints("test");
        p.setFunctionName(functionName);
        p.setReturnType(returnType);
        p.setParams(params);
        return p;
    }

    @Test
    void pythonStarterForIntArrayReturn() {
        Problem problem = createProblem("twoSum", "int[]",
                "[{\"name\":\"nums\",\"type\":\"int[]\"},{\"name\":\"target\",\"type\":\"int\"}]");

        String result = service.generateStarter(problem, Language.PYTHON);

        assertThat(result).contains("def twoSum(nums: list[int], target: int) -> list[int]:");
        assertThat(result).contains("pass");
    }

    @Test
    void javaStarterForIntArrayReturn() {
        Problem problem = createProblem("twoSum", "int[]",
                "[{\"name\":\"nums\",\"type\":\"int[]\"},{\"name\":\"target\",\"type\":\"int\"}]");

        String result = service.generateStarter(problem, Language.JAVA);

        assertThat(result).contains("public int[] twoSum(int[] nums, int target)");
        assertThat(result).contains("class Solution");
    }

    @Test
    void cppStarterForIntArrayReturn() {
        Problem problem = createProblem("twoSum", "int[]",
                "[{\"name\":\"nums\",\"type\":\"int[]\"},{\"name\":\"target\",\"type\":\"int\"}]");

        String result = service.generateStarter(problem, Language.CPP);

        assertThat(result).contains("vector<int> twoSum(vector<int>& nums, int target)");
        assertThat(result).contains("class Solution");
    }

    @Test
    void pythonStarterForBoolReturn() {
        Problem problem = createProblem("isPalindrome", "bool",
                "[{\"name\":\"s\",\"type\":\"string\"}]");

        String result = service.generateStarter(problem, Language.PYTHON);

        assertThat(result).contains("def isPalindrome(s: str) -> bool:");
    }

    @Test
    void pythonStarterForVoidReturn() {
        Problem problem = createProblem("reverseList", "void",
                "[{\"name\":\"head\",\"type\":\"ListNode\"}]");

        String result = service.generateStarter(problem, Language.PYTHON);

        assertThat(result).contains("def reverseList(head: ListNode) -> None:");
    }
}
