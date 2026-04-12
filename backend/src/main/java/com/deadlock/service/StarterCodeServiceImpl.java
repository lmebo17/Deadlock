package com.deadlock.service;

import com.deadlock.model.Language;
import com.deadlock.model.Problem;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class StarterCodeServiceImpl implements StarterCodeService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public String generateStarter(Problem problem, Language language) {
        List<Map<String, String>> params = parseParams(problem.getParams());
        String functionName = problem.getFunctionName();
        String returnType = problem.getReturnType();

        return switch (language) {
            case PYTHON -> generatePython(functionName, returnType, params);
            case JAVA -> generateJava(functionName, returnType, params);
            case CPP -> generateCpp(functionName, returnType, params);
        };
    }

    private List<Map<String, String>> parseParams(String paramsJson) {
        try {
            return MAPPER.readValue(paramsJson, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid params JSON: " + paramsJson, e);
        }
    }

    // ── Python ──────────────────────────────────────────────────────────────

    private String generatePython(String functionName, String returnType, List<Map<String, String>> params) {
        StringBuilder sb = new StringBuilder();
        sb.append("def ").append(functionName).append("(");

        for (int i = 0; i < params.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(params.get(i).get("name"))
              .append(": ")
              .append(toPythonType(params.get(i).get("type")));
        }

        sb.append(") -> ").append(toPythonType(returnType)).append(":\n");
        sb.append("    pass\n");
        return sb.toString();
    }

    private String toPythonType(String type) {
        return switch (type) {
            case "int" -> "int";
            case "float" -> "float";
            case "bool" -> "bool";
            case "string" -> "str";
            case "int[]" -> "list[int]";
            case "string[]" -> "list[str]";
            case "int[][]" -> "list[list[int]]";
            case "void" -> "None";
            default -> type; // ListNode, TreeNode, etc.
        };
    }

    // ── Java ────────────────────────────────────────────────────────────────

    private String generateJava(String functionName, String returnType, List<Map<String, String>> params) {
        StringBuilder sb = new StringBuilder();
        sb.append("class Solution {\n");
        sb.append("    public ").append(toJavaType(returnType)).append(" ").append(functionName).append("(");

        for (int i = 0; i < params.size(); i++) {
            if (i > 0) sb.append(", ");
            String pType = params.get(i).get("type");
            sb.append(toJavaType(pType)).append(" ").append(params.get(i).get("name"));
        }

        sb.append(") {\n");
        sb.append("        return ").append(javaDefaultReturn(returnType)).append(";\n");
        sb.append("    }\n");
        sb.append("}\n");
        return sb.toString();
    }

    private String toJavaType(String type) {
        return switch (type) {
            case "int" -> "int";
            case "float" -> "double";
            case "bool" -> "boolean";
            case "string" -> "String";
            case "int[]" -> "int[]";
            case "string[]" -> "String[]";
            case "int[][]" -> "int[][]";
            case "void" -> "void";
            default -> type; // ListNode, TreeNode, etc.
        };
    }

    private String javaDefaultReturn(String type) {
        return switch (type) {
            case "int" -> "0";
            case "float" -> "0.0";
            case "bool" -> "false";
            case "string" -> "\"\"";
            case "int[]" -> "new int[]{}";
            case "string[]" -> "new String[]{}";
            case "int[][]" -> "new int[][]{}";
            case "ListNode", "TreeNode" -> "null";
            default -> "null";
        };
    }

    // ── C++ ─────────────────────────────────────────────────────────────────

    private String generateCpp(String functionName, String returnType, List<Map<String, String>> params) {
        StringBuilder sb = new StringBuilder();
        sb.append("class Solution {\n");
        sb.append("public:\n");
        sb.append("    ").append(toCppType(returnType)).append(" ").append(functionName).append("(");

        for (int i = 0; i < params.size(); i++) {
            if (i > 0) sb.append(", ");
            String pType = params.get(i).get("type");
            sb.append(toCppType(pType));
            if (isCppRefType(pType)) sb.append("&");
            sb.append(" ").append(params.get(i).get("name"));
        }

        sb.append(") {\n");
        String defaultVal = cppDefaultReturn(returnType);
        if (!defaultVal.isEmpty()) {
            sb.append("        return ").append(defaultVal).append(";\n");
        }
        sb.append("    }\n");
        sb.append("};\n");
        return sb.toString();
    }

    private String toCppType(String type) {
        return switch (type) {
            case "int" -> "int";
            case "float" -> "double";
            case "bool" -> "bool";
            case "string" -> "string";
            case "int[]" -> "vector<int>";
            case "string[]" -> "vector<string>";
            case "int[][]" -> "vector<vector<int>>";
            case "void" -> "void";
            case "ListNode" -> "ListNode*";
            case "TreeNode" -> "TreeNode*";
            default -> type;
        };
    }

    private boolean isCppRefType(String type) {
        return switch (type) {
            case "int[]", "string[]", "int[][]", "string" -> true;
            default -> false;
        };
    }

    private String cppDefaultReturn(String type) {
        return switch (type) {
            case "int" -> "0";
            case "float" -> "0.0";
            case "bool" -> "false";
            case "string" -> "\"\"";
            case "int[]", "string[]", "int[][]" -> "{}";
            case "ListNode", "TreeNode" -> "nullptr";
            case "void" -> "";
            default -> "{}";
        };
    }
}
