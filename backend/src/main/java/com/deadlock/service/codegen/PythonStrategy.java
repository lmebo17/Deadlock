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
public class PythonStrategy implements LanguageWrapperStrategy {

    @Override
    public Language getLanguage() {
        return Language.PYTHON;
    }

    @Override
    public String wrapCode(String userCode, String functionName, List<ParamInfo> params, String returnType) {
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
            String name = params.get(i).name();
            String type = params.get(i).type();
            sb.append("    _p").append(i).append(" = json.loads(_lines[").append(i).append("])\n");
            sb.append("    ").append(name).append(" = _deser(\"").append(type).append("\", _p").append(i).append(")\n");
        }

        // Call the function
        String argList = buildArgList(params);
        boolean isVoid = "void".equals(returnType);

        if (isVoid) {
            sb.append("    ").append(functionName).append("(").append(argList).append(")\n");
            // Serialize the first parameter (in-place modification)
            if (!params.isEmpty()) {
                String firstParamName = params.get(0).name();
                String firstParamType = params.get(0).type();
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

    @Override
    public String generateStarter(String functionName, List<ParamInfo> params, String returnType) {
        StringBuilder sb = new StringBuilder();
        sb.append("def ").append(functionName).append("(");

        for (int i = 0; i < params.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(params.get(i).name())
              .append(": ")
              .append(toPythonType(params.get(i).type()));
        }

        sb.append(") -> ").append(toPythonType(returnType)).append(":\n");
        sb.append("    pass\n");
        return sb.toString();
    }

    private String buildArgList(List<ParamInfo> params) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < params.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(params.get(i).name());
        }
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

    private String loadResource(String path) {
        try (InputStream is = new ClassPathResource(path).getInputStream()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load resource: " + path, e);
        }
    }
}
