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
public class CppStrategy implements LanguageWrapperStrategy {

    @Override
    public Language getLanguage() {
        return Language.CPP;
    }

    @Override
    public String wrapCode(String userCode, String functionName, List<ParamInfo> params, String returnType) {
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
            String name = params.get(i).name();
            String type = params.get(i).type();
            sb.append("    string _line").append(i).append(";\n");
            sb.append("    getline(cin, _line").append(i).append(");\n");
            sb.append("    ").append(cppParseParam(type, name, "_line" + i));
        }

        // Call the function
        String argList = buildArgList(params);
        boolean isVoid = "void".equals(returnType);

        if (isVoid) {
            sb.append("    sol.").append(functionName).append("(").append(argList).append(");\n");
            if (!params.isEmpty()) {
                String firstName = params.get(0).name();
                String firstType = params.get(0).type();
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

    @Override
    public String generateStarter(String functionName, List<ParamInfo> params, String returnType) {
        StringBuilder sb = new StringBuilder();
        sb.append("class Solution {\n");
        sb.append("public:\n");
        sb.append("    ").append(toCppType(returnType)).append(" ").append(functionName).append("(");

        for (int i = 0; i < params.size(); i++) {
            if (i > 0) sb.append(", ");
            String pType = params.get(i).type();
            sb.append(toCppType(pType));
            if (isCppRefType(pType)) sb.append("&");
            sb.append(" ").append(params.get(i).name());
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

    private String cppParseParam(String type, String varName, String lineVar) {
        return switch (type) {
            case "int" -> "int " + varName + " = _json::parseInt(" + lineVar + ");\n";
            case "long" -> "long long " + varName + " = _json::parseLong(" + lineVar + ");\n";
            case "float" -> "double " + varName + " = _json::parseFloat(" + lineVar + ");\n";
            case "bool" -> "bool " + varName + " = _json::parseBool(" + lineVar + ");\n";
            case "string" -> "string " + varName + " = _json::parseString(" + lineVar + ");\n";
            case "int[]" -> "vector<int> " + varName + " = _json::parseIntArray(" + lineVar + ");\n";
            case "long[]" -> "vector<long long> " + varName + " = _json::parseLongArray(" + lineVar + ");\n";
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
            case "long" -> "_json::serializeLong(" + varName + ")";
            case "float" -> "_json::serializeFloat(" + varName + ")";
            case "bool" -> "_json::serializeBool(" + varName + ")";
            case "string" -> "_json::serializeString(" + varName + ")";
            case "int[]" -> "_json::serializeIntArray(" + varName + ")";
            case "long[]" -> "_json::serializeLongArray(" + varName + ")";
            case "string[]" -> "_json::serializeStringArray(" + varName + ")";
            case "int[][]" -> "_json::serializeInt2DArray(" + varName + ")";
            case "ListNode" -> "_json::serializeListNode(" + varName + ")";
            case "TreeNode" -> "_json::serializeTreeNode(" + varName + ")";
            default -> "\"\" + " + varName;
        };
    }

    private String toCppType(String type) {
        return switch (type) {
            case "int" -> "int";
            case "long" -> "long long";
            case "float" -> "double";
            case "bool" -> "bool";
            case "string" -> "string";
            case "int[]" -> "vector<int>";
            case "long[]" -> "vector<long long>";
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
            case "int[]", "long[]", "string[]", "int[][]", "string" -> true;
            default -> false;
        };
    }

    private String cppDefaultReturn(String type) {
        return switch (type) {
            case "int" -> "0";
            case "long" -> "0LL";
            case "float" -> "0.0";
            case "bool" -> "false";
            case "string" -> "\"\"";
            case "int[]", "long[]", "string[]", "int[][]" -> "{}";
            case "ListNode", "TreeNode" -> "nullptr";
            case "void" -> "";
            default -> "{}";
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
