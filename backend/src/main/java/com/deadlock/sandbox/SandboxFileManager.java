package com.deadlock.sandbox;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class SandboxFileManager {

    public Path createSandboxDir(String code, String language, List<String> testInputs) throws IOException {
        Path tempDir = Files.createTempDirectory("deadlock-sandbox-");
        Path testsDir = tempDir.resolve("tests");
        Path resultsDir = tempDir.resolve("results");
        Files.createDirectories(testsDir);
        Files.createDirectories(resultsDir);

        // Write code file
        String filename = getCodeFilename(language, code);
        Files.writeString(tempDir.resolve(filename), code);

        // Write all test inputs
        for (int i = 0; i < testInputs.size(); i++) {
            String idx = String.format("%02d", i + 1);
            Files.writeString(testsDir.resolve(idx + "-input.txt"), testInputs.get(i));
        }

        // Make everything accessible by sandbox user (uid 1000)
        setPermissionsRecursive(tempDir);

        return tempDir;
    }

    public String getCodeFilename(String language, String code) {
        String ext = switch (language.toUpperCase()) {
            case "JAVA" -> "java";
            case "PYTHON" -> "py";
            case "CPP" -> "cpp";
            default -> "txt";
        };
        if ("JAVA".equalsIgnoreCase(language) && code.contains("class Main")) {
            return "Main.java";
        }
        return "solution." + ext;
    }

    public List<SandboxResult.TestCaseResult> readResults(Path tempDir, int testCount) throws IOException {
        Path resultsDir = tempDir.resolve("results");
        List<SandboxResult.TestCaseResult> results = new ArrayList<>();

        for (int i = 0; i < testCount; i++) {
            String idx = String.format("%02d", i + 1);
            Path outputFile = resultsDir.resolve(idx + "-output.txt");
            Path exitFile = resultsDir.resolve(idx + "-exit.txt");
            Path errorFile = resultsDir.resolve(idx + "-error.txt");

            String stdout = Files.exists(outputFile)
                    ? Files.readString(outputFile, StandardCharsets.UTF_8) : "";
            String stderr = Files.exists(errorFile)
                    ? Files.readString(errorFile, StandardCharsets.UTF_8) : "";
            int exitCode = 0;
            boolean timedOut = false;

            if (Files.exists(exitFile)) {
                exitCode = Integer.parseInt(Files.readString(exitFile, StandardCharsets.UTF_8).trim());
                timedOut = (exitCode == 124);
            } else {
                // No exit file means container died before reaching this test (OOM/TLE on earlier test)
                break;
            }

            results.add(new SandboxResult.TestCaseResult(i + 1, exitCode, stdout, stderr, timedOut));
        }

        return results;
    }

    public SandboxResult readCompileError(Path tempDir, long totalTime) throws IOException {
        Path resultsDir = tempDir.resolve("results");
        Path compileErrorFile = resultsDir.resolve("compile_error.txt");
        Path statusFile = resultsDir.resolve("status.txt");

        if (Files.exists(statusFile)) {
            String status = Files.readString(statusFile, StandardCharsets.UTF_8).trim();
            if ("COMPILE_ERROR".equals(status)) {
                String compileErr = Files.exists(compileErrorFile)
                        ? Files.readString(compileErrorFile, StandardCharsets.UTF_8) : "";
                return new SandboxResult(true, compileErr, List.of(), totalTime, false);
            }
        }
        return null;
    }

    public void cleanup(Path tempDir) {
        deleteRecursive(tempDir);
    }

    private void setPermissionsRecursive(Path dir) {
        dir.toFile().setReadable(true, false);
        dir.toFile().setWritable(true, false);
        dir.toFile().setExecutable(true, false);
        var files = dir.toFile().listFiles();
        if (files != null) {
            for (var f : files) {
                f.setReadable(true, false);
                f.setWritable(true, false);
                if (f.isDirectory()) {
                    f.setExecutable(true, false);
                    setPermissionsRecursive(f.toPath());
                }
            }
        }
    }

    private void deleteRecursive(Path dir) {
        try {
            var files = dir.toFile().listFiles();
            if (files != null) {
                for (var f : files) {
                    if (f.isDirectory()) deleteRecursive(f.toPath());
                    else f.delete();
                }
            }
            dir.toFile().delete();
        } catch (Exception e) {
            log.warn("Failed to clean up temp directory {}", dir, e);
        }
    }
}
