package com.deadlock.service.codegen;

import com.deadlock.model.Language;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Component
public class JavaStrategy implements LanguageWrapperStrategy {

    @Override
    public Language getLanguage() {
        return Language.JAVA;
    }

    @Override
    public String wrapCode(String userCode, String functionName, List<ParamInfo> params, String returnType) {
        StringBuilder sb = new StringBuilder();

        sb.append("import java.util.*;\n");
        sb.append("import java.io.*;\n");
        sb.append("import java.util.stream.*;\n\n");

        // 1. ListNode class (package-private)
        sb.append(makePackagePrivate(loadResource("harness/java_harness/ListNode.java")));
        sb.append("\n\n");

        // 2. TreeNode class (package-private)
        String treeNodeSource = loadResource("harness/java_harness/TreeNode.java");
        // Remove the import line since we already have it at the top
        treeNodeSource = treeNodeSource.replaceAll("import java\\.util\\.\\*;\\s*\\n?", "");
        sb.append(makePackagePrivate(treeNodeSource));
        sb.append("\n\n");

        // 3. User code (make package-private)
        sb.append(makePackagePrivate(userCode));
        sb.append("\n\n");

        // 4. Main class
        sb.append("class Main {\n");
        sb.append("    public static void main(String[] args) throws Exception {\n");
        sb.append("        BufferedReader _br = new BufferedReader(new InputStreamReader(System.in));\n");

        // Read and parse each parameter
        for (int i = 0; i < params.size(); i++) {
            String name = params.get(i).name();
            String type = params.get(i).type();
            sb.append("        String _line").append(i).append(" = _br.readLine().trim();\n");
            sb.append("        ").append(javaParseParam(type, name, "_line" + i));
        }

        // Call the function
        sb.append("        Solution _sol = new Solution();\n");
        String argList = buildArgList(params);
        boolean isVoid = "void".equals(returnType);

        if (isVoid) {
            sb.append("        _sol.").append(functionName).append("(").append(argList).append(");\n");
            if (!params.isEmpty()) {
                String firstName = params.get(0).name();
                String firstType = params.get(0).type();
                sb.append("        System.out.println(").append(javaSerialize(firstType, firstName)).append(");\n");
            }
        } else {
            sb.append("        ").append(toJavaType(returnType)).append(" _result = _sol.")
              .append(functionName).append("(").append(argList).append(");\n");
            sb.append("        System.out.println(").append(javaSerialize(returnType, "_result")).append(");\n");
        }

        sb.append("    }\n");
        sb.append("}\n");

        return sb.toString();
    }

    @Override
    public String generateStarter(String functionName, List<ParamInfo> params, String returnType) {
        StringBuilder sb = new StringBuilder();
        sb.append("class Solution {\n");
        sb.append("    public ").append(toJavaType(returnType)).append(" ").append(functionName).append("(");

        for (int i = 0; i < params.size(); i++) {
            if (i > 0) sb.append(", ");
            String pType = params.get(i).type();
            sb.append(toJavaType(pType)).append(" ").append(params.get(i).name());
        }

        sb.append(") {\n");
        sb.append("        return ").append(javaDefaultReturn(returnType)).append(";\n");
        sb.append("    }\n");
        sb.append("}\n");
        return sb.toString();
    }

    private String makePackagePrivate(String source) {
        return source.replaceAll("public\\s+class\\s+", "class ");
    }

    private String javaParseParam(String type, String varName, String lineVar) {
        return switch (type) {
            case "int" -> toJavaType(type) + " " + varName + " = Integer.parseInt(" + lineVar + ");\n";
            case "long" -> toJavaType(type) + " " + varName + " = Long.parseLong(" + lineVar + ");\n";
            case "float" -> toJavaType(type) + " " + varName + " = Double.parseDouble(" + lineVar + ");\n";
            case "bool" -> toJavaType(type) + " " + varName + " = Boolean.parseBoolean(" + lineVar + ");\n";
            case "string" -> toJavaType(type) + " " + varName + " = " + lineVar
                    + ".substring(1, " + lineVar + ".length() - 1);\n";
            case "int[]" -> toJavaType(type) + " " + varName + " = Arrays.stream("
                    + lineVar + ".replaceAll(\"[\\\\[\\\\]]\", \"\").split(\",\"))"
                    + ".map(String::trim).filter(s -> !s.isEmpty()).mapToInt(Integer::parseInt).toArray();\n";
            case "long[]" -> toJavaType(type) + " " + varName + " = Arrays.stream("
                    + lineVar + ".replaceAll(\"[\\\\[\\\\]]\", \"\").split(\",\"))"
                    + ".map(String::trim).filter(s -> !s.isEmpty()).mapToLong(Long::parseLong).toArray();\n";
            case "string[]" -> toJavaType(type) + " " + varName + " = Arrays.stream("
                    + lineVar + ".replaceAll(\"[\\\\[\\\\]]\", \"\").split(\",\"))"
                    + ".map(s -> s.trim().replaceAll(\"\\\"\", \"\")).toArray(String[]::new);\n";
            case "int[][]" -> toJavaType(type) + " " + varName + " = _parseIntArray2D(" + lineVar + ");\n";
            default -> toJavaType(type) + " " + varName + " = null; // TODO: parse " + type + "\n";
        };
    }

    private String javaSerialize(String type, String varName) {
        return switch (type) {
            case "int", "long" -> "\"\" + " + varName;
            case "float" -> "\"\" + " + varName;
            case "bool" -> "\"\" + " + varName;
            case "string" -> "\"\\\"\" + " + varName + " + \"\\\"\"";
            case "int[]" -> "Arrays.toString(" + varName + ").replace(\" \", \"\")";
            case "long[]" -> "Arrays.toString(" + varName + ").replace(\" \", \"\")";
            case "string[]" -> "\"[\" + Arrays.stream(" + varName
                    + ").map(s -> \"\\\"\" + s + \"\\\"\").collect(Collectors.joining(\",\")) + \"]\"";
            case "int[][]" -> "\"[\" + Arrays.stream(" + varName
                    + ").map(a -> Arrays.toString(a).replace(\" \", \"\")).collect(Collectors.joining(\",\")) + \"]\"";
            default -> "\"\" + " + varName; // fallback
        };
    }

    private String toJavaType(String type) {
        return switch (type) {
            case "int" -> "int";
            case "long" -> "long";
            case "float" -> "double";
            case "bool" -> "boolean";
            case "string" -> "String";
            case "int[]" -> "int[]";
            case "long[]" -> "long[]";
            case "string[]" -> "String[]";
            case "int[][]" -> "int[][]";
            case "void" -> "void";
            default -> type; // ListNode, TreeNode, etc.
        };
    }

    private String javaDefaultReturn(String type) {
        return switch (type) {
            case "int" -> "0";
            case "long" -> "0L";
            case "float" -> "0.0";
            case "bool" -> "false";
            case "string" -> "\"\"";
            case "int[]" -> "new int[]{}";
            case "long[]" -> "new long[]{}";
            case "string[]" -> "new String[]{}";
            case "int[][]" -> "new int[][]{}";
            case "ListNode", "TreeNode" -> "null";
            default -> "null";
        };
    }

    private String buildArgList(List<ParamInfo> params) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < params.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(params.get(i).name());
        }
        return sb.toString();
    }

    private String loadResource(String path) {
        try (InputStream is = new ClassPathResource(path).getInputStream()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load resource: " + path, e);
        }
    }
}
