package com.deadlock.service;

import com.deadlock.model.Language;
import com.deadlock.model.Problem;
import com.deadlock.service.codegen.CppStrategy;
import com.deadlock.service.codegen.JavaStrategy;
import com.deadlock.service.codegen.LanguageWrapperStrategy;
import com.deadlock.service.codegen.PythonStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WrapperCodeServiceTest {

    private WrapperCodeServiceImpl service;

    @BeforeEach
    void setUp() {
        List<LanguageWrapperStrategy> strategies = List.of(
                new PythonStrategy(),
                new JavaStrategy(),
                new CppStrategy()
        );
        service = new WrapperCodeServiceImpl(strategies);
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

    // ── Python ──────────────────────────────────────────────────────────────

    @Test
    void pythonWrapperContainsUserCode() {
        Problem problem = createProblem("twoSum", "int[]",
                "[{\"name\":\"nums\",\"type\":\"int[]\"},{\"name\":\"target\",\"type\":\"int\"}]");

        String userCode = "def twoSum(nums, target):\n    return [0, 1]\n";
        String result = service.wrapCode(userCode, problem, Language.PYTHON);

        // Harness classes present
        assertThat(result).contains("class ListNode:");
        assertThat(result).contains("class TreeNode:");
        assertThat(result).contains("def _deser(");
        assertThat(result).contains("def _ser(");

        // User code present
        assertThat(result).contains("def twoSum(nums, target):");
        assertThat(result).contains("return [0, 1]");

        // Main block present with deserialization and serialization
        assertThat(result).contains("if __name__ == \"__main__\":");
        assertThat(result).contains("json.loads");
        assertThat(result).contains("json.dumps");
    }

    @Test
    void pythonWrapperHandlesVoidReturn() {
        Problem problem = createProblem("reverseList", "void",
                "[{\"name\":\"head\",\"type\":\"ListNode\"}]");

        String userCode = "def reverseList(head):\n    pass\n";
        String result = service.wrapCode(userCode, problem, Language.PYTHON);

        // For void return, should serialize the first parameter (in-place modification)
        assertThat(result).contains("if __name__ == \"__main__\":");
        assertThat(result).contains("_ser(\"ListNode\"");
        // Should NOT capture return value into _result with assignment for use
        assertThat(result).doesNotContain("_result = reverseList(");
    }

    // ── Java ────────────────────────────────────────────────────────────────

    @Test
    void javaWrapperContainsUserCode() {
        Problem problem = createProblem("twoSum", "int[]",
                "[{\"name\":\"nums\",\"type\":\"int[]\"},{\"name\":\"target\",\"type\":\"int\"}]");

        String userCode = "class Solution {\n    public int[] twoSum(int[] nums, int target) {\n        return new int[]{0,1};\n    }\n}\n";
        String result = service.wrapCode(userCode, problem, Language.JAVA);

        // Harness classes present
        assertThat(result).contains("class ListNode");
        assertThat(result).contains("class TreeNode");

        // User code present
        assertThat(result).contains("twoSum(int[] nums, int target)");

        // Main class present
        assertThat(result).contains("class Main");
        assertThat(result).contains("public static void main(String[] args)");
    }

    // ── C++ ─────────────────────────────────────────────────────────────────

    @Test
    void cppWrapperContainsUserCode() {
        Problem problem = createProblem("twoSum", "int[]",
                "[{\"name\":\"nums\",\"type\":\"int[]\"},{\"name\":\"target\",\"type\":\"int\"}]");

        String userCode = "class Solution {\npublic:\n    vector<int> twoSum(vector<int>& nums, int target) {\n        return {0,1};\n    }\n};\n";
        String result = service.wrapCode(userCode, problem, Language.CPP);

        // Harness content present
        assertThat(result).contains("namespace _json");
        assertThat(result).contains("struct ListNode");
        assertThat(result).contains("struct TreeNode");

        // User code present
        assertThat(result).contains("vector<int> twoSum(");

        // main() present
        assertThat(result).contains("int main()");
        assertThat(result).contains("getline");
    }
}
