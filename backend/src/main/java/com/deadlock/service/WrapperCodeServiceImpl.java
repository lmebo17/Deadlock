package com.deadlock.service;

import com.deadlock.model.Language;
import com.deadlock.model.Problem;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
public class WrapperCodeServiceImpl implements WrapperCodeService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public String wrapCode(String userCode, Problem problem, Language language) {
        List<Map<String, String>> params = parseParams(problem.getParams());
        String functionName = problem.getFunctionName();
        String returnType = problem.getReturnType();

        return switch (language) {
            case PYTHON -> wrapPython(userCode, functionName, returnType, params);
            case JAVA -> wrapJava(userCode, functionName, returnType, params);
            case CPP -> wrapCpp(userCode, functionName, returnType, params);
        };
    }

    private List<Map<String, String>> parseParams(String paramsJson) {
        try {
            return MAPPER.readValue(paramsJson, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid params JSON: " + paramsJson, e);
        }
    }

    private String loadResource(String path) {
        try (InputStream is = new ClassPathResource(path).getInputStream()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load resource: " + path, e);
        }
    }

    // ── Python ──────────────────────────────────────────────────────────────

    private String wrapPython(String userCode, String functionName,
                              String returnType, List<Map<String, String>> params) {
        StringBuilder sb = new StringBuilder();

        // 1. Harness (ListNode, TreeNode, helpers, _deser, _ser)
        sb.append(loadResource("harness/python_harness.py"));
        sb.append("\n");

        // 2. User code
        sb.append(userCode);
        sb.append("\n");

        // 3. Main block
        sb.append("if __name__ == \"__main__\":\n");
        sb.append("    import sys\n");
        sb.append("    _lines = [line.strip() for line in sys.stdin if line.strip()]\n");

        // Read each parameter from a JSON line
        for (int i = 0; i < params.size(); i++) {
            String name = params.get(i).get("name");
            String type = params.get(i).get("type");
            sb.append("    _p").append(i).append(" = json.loads(_lines[").append(i).append("])\n");
            sb.append("    ").append(name).append(" = _deser(\"").append(type).append("\", _p").append(i).append(")\n");
        }

        // Call the function
        String argList = buildPythonArgList(params);
        boolean isVoid = "void".equals(returnType);

        if (isVoid) {
            sb.append("    ").append(functionName).append("(").append(argList).append(")\n");
            // Serialize the first parameter (in-place modification)
            if (!params.isEmpty()) {
                String firstParamName = params.get(0).get("name");
                String firstParamType = params.get(0).get("type");
                sb.append("    _out = _ser(\"").append(firstParamType).append("\", ").append(firstParamName).append(")\n");
                sb.append("    print(json.dumps(_out))\n");
            }
        } else {
            sb.append("    _result = ").append(functionName).append("(").append(argList).append(")\n");
            sb.append("    _out = _ser(\"").append(returnType).append("\", _result)\n");
            sb.append("    print(json.dumps(_out))\n");
        }

        return sb.toString();
    }

    private String buildPythonArgList(List<Map<String, String>> params) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < params.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(params.get(i).get("name"));
        }
        return sb.toString();
    }

    // ── Java ────────────────────────────────────────────────────────────────

    private String wrapJava(String userCode, String functionName,
                            String returnType, List<Map<String, String>> params) {
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
            String name = params.get(i).get("name");
            String type = params.get(i).get("type");
            sb.append("        String _line").append(i).append(" = _br.readLine().trim();\n");
            sb.append("        ").append(javaParseParam(type, name, "_line" + i));
        }

        // Call the function
        sb.append("        Solution _sol = new Solution();\n");
        String argList = buildJavaArgList(params);
        boolean isVoid = "void".equals(returnType);

        if (isVoid) {
            sb.append("        _sol.").append(functionName).append("(").append(argList).append(");\n");
            if (!params.isEmpty()) {
                String firstName = params.get(0).get("name");
                String firstType = params.get(0).get("type");
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

    private String makePackagePrivate(String source) {
        return source.replaceAll("public\\s+class\\s+", "class ");
    }

    private String javaParseParam(String type, String varName, String lineVar) {
        return switch (type) {
            case "int" -> toJavaType(type) + " " + varName + " = Integer.parseInt(" + lineVar + ");\n";
            case "float" -> toJavaType(type) + " " + varName + " = Double.parseDouble(" + lineVar + ");\n";
            case "bool" -> toJavaType(type) + " " + varName + " = Boolean.parseBoolean(" + lineVar + ");\n";
            case "string" -> toJavaType(type) + " " + varName + " = " + lineVar
                    + ".substring(1, " + lineVar + ".length() - 1);\n";
            case "int[]" -> toJavaType(type) + " " + varName + " = Arrays.stream("
                    + lineVar + ".replaceAll(\"[\\\\[\\\\]]\", \"\").split(\",\"))"
                    + ".map(String::trim).filter(s -> !s.isEmpty()).mapToInt(Integer::parseInt).toArray();\n";
            case "string[]" -> toJavaType(type) + " " + varName + " = Arrays.stream("
                    + lineVar + ".replaceAll(\"[\\\\[\\\\]]\", \"\").split(\",\"))"
                    + ".map(s -> s.trim().replaceAll(\"\\\"\", \"\")).toArray(String[]::new);\n";
            case "int[][]" -> toJavaType(type) + " " + varName + " = _parseIntArray2D(" + lineVar + ");\n";
            default -> toJavaType(type) + " " + varName + " = null; // TODO: parse " + type + "\n";
        };
    }

    private String javaSerialize(String type, String varName) {
        return switch (type) {
            case "int" -> "\"\" + " + varName;
            case "float" -> "\"\" + " + varName;
            case "bool" -> "\"\" + " + varName;
            case "string" -> "\"\\\"\" + " + varName + " + \"\\\"\"";
            case "int[]" -> "Arrays.toString(" + varName + ").replace(\" \", \"\")";
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

    private String buildJavaArgList(List<Map<String, String>> params) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < params.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(params.get(i).get("name"));
        }
        return sb.toString();
    }

    // ── C++ ─────────────────────────────────────────────────────────────────

    private String wrapCpp(String userCode, String functionName,
                           String returnType, List<Map<String, String>> params) {
        StringBuilder sb = new StringBuilder();

        // 1. Harness header (includes, structs, _json namespace)
        sb.append(loadResource("harness/cpp_harness.h"));
        sb.append("\n");

        // 2. User code
        sb.append(userCode);
        sb.append("\n");

        // 3. main()
        sb.append("int main() {\n");
        sb.append("    Solution sol;\n");

        // Read each parameter
        for (int i = 0; i < params.size(); i++) {
            String name = params.get(i).get("name");
            String type = params.get(i).get("type");
            sb.append("    string _line").append(i).append(";\n");
            sb.append("    getline(cin, _line").append(i).append(");\n");
            sb.append("    ").append(cppParseParam(type, name, "_line" + i));
        }

        // Call the function
        String argList = buildCppArgList(params);
        boolean isVoid = "void".equals(returnType);

        if (isVoid) {
            sb.append("    sol.").append(functionName).append("(").append(argList).append(");\n");
            if (!params.isEmpty()) {
                String firstName = params.get(0).get("name");
                String firstType = params.get(0).get("type");
                sb.append("    cout << ").append(cppSerialize(firstType, firstName)).append(" << endl;\n");
            }
        } else {
            sb.append("    auto _result = sol.").append(functionName).append("(").append(argList).append(");\n");
            sb.append("    cout << ").append(cppSerialize(returnType, "_result")).append(" << endl;\n");
        }

        sb.append("    return 0;\n");
        sb.append("}\n");

        return sb.toString();
    }

    private String cppParseParam(String type, String varName, String lineVar) {
        return switch (type) {
            case "int" -> "int " + varName + " = _json::parseInt(" + lineVar + ");\n";
            case "float" -> "double " + varName + " = _json::parseFloat(" + lineVar + ");\n";
            case "bool" -> "bool " + varName + " = _json::parseBool(" + lineVar + ");\n";
            case "string" -> "string " + varName + " = _json::parseString(" + lineVar + ");\n";
            case "int[]" -> "vector<int> " + varName + " = _json::parseIntArray(" + lineVar + ");\n";
            case "string[]" -> "vector<string> " + varName + " = _json::parseStringArray(" + lineVar + ");\n";
            case "int[][]" -> "vector<vector<int>> " + varName + " = _json::parseInt2DArray(" + lineVar + ");\n";
            case "ListNode" -> "ListNode* " + varName + " = _json::parseListNode(" + lineVar + ");\n";
            case "TreeNode" -> "TreeNode* " + varName + " = _json::parseTreeNode(" + lineVar + ");\n";
            default -> "auto " + varName + " = " + lineVar + "; // TODO: parse " + type + "\n";
        };
    }

    private String cppSerialize(String type, String varName) {
        return switch (type) {
            case "int" -> "_json::serializeInt(" + varName + ")";
            case "float" -> "_json::serializeFloat(" + varName + ")";
            case "bool" -> "_json::serializeBool(" + varName + ")";
            case "string" -> "_json::serializeString(" + varName + ")";
            case "int[]" -> "_json::serializeIntArray(" + varName + ")";
            case "string[]" -> "_json::serializeStringArray(" + varName + ")";
            case "int[][]" -> "_json::serializeInt2DArray(" + varName + ")";
            case "ListNode" -> "_json::serializeListNode(" + varName + ")";
            case "TreeNode" -> "_json::serializeTreeNode(" + varName + ")";
            default -> "\"\" + " + varName;
        };
    }

    private String buildCppArgList(List<Map<String, String>> params) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < params.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(params.get(i).get("name"));
        }
        return sb.toString();
    }
}
