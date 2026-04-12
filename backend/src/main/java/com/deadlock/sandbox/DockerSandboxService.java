package com.deadlock.sandbox;

import com.deadlock.model.Language;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.WaitContainerResultCallback;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Capability;
import com.github.dockerjava.api.model.HostConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class DockerSandboxService implements SandboxService {

    private final DockerClient dockerClient;

    @Override
    public SandboxResult executeAll(String code, String language, List<String> testInputs,
                                     int timeLimitMs, int memoryLimitMb) {
        Language lang;
        try {
            lang = Language.valueOf(language.toUpperCase());
        } catch (IllegalArgumentException e) {
            return new SandboxResult(false, "Unsupported language: " + language,
                    List.of(), 0, false);
        }
        String image = lang.getDockerImage();
        String ext = lang.getFileExtension();

        Path tempDir = null;
        String containerId = null;
        try {
            // Set up temp directory
            tempDir = Files.createTempDirectory("deadlock-sandbox-");
            Path testsDir = tempDir.resolve("tests");
            Path resultsDir = tempDir.resolve("results");
            Files.createDirectories(testsDir);
            Files.createDirectories(resultsDir);

            // Write code file
            String filename = "solution." + ext;
            if ("JAVA".equalsIgnoreCase(language) && code.contains("class Main")) {
                filename = "Main.java";
            }
            Files.writeString(tempDir.resolve(filename), code);

            // Write all test inputs
            for (int i = 0; i < testInputs.size(); i++) {
                String idx = String.format("%02d", i + 1);
                Files.writeString(testsDir.resolve(idx + "-input.txt"), testInputs.get(i));
            }

            // Make everything accessible by sandbox user (uid 1000)
            setPermissionsRecursive(tempDir);

            int timeLimitSec = Math.max(1, timeLimitMs / 1000);
            int overallTimeoutSec = Math.min(timeLimitSec * testInputs.size() + 10, 120);

            // Create container
            HostConfig hostConfig = HostConfig.newHostConfig()
                    .withMemory((long) memoryLimitMb * 1024 * 1024)
                    .withMemorySwap((long) memoryLimitMb * 1024 * 1024)
                    .withCpuCount(1L)
                    .withPidsLimit(64L)
                    .withNetworkMode("none")
                    .withBinds(Bind.parse(tempDir.toAbsolutePath() + ":/code"))
                    .withCapDrop(Capability.values());

            CreateContainerResponse container = dockerClient.createContainerCmd(image)
                    .withHostConfig(hostConfig)
                    .withEnv("TIME_LIMIT_SEC=" + timeLimitSec,
                             "MEMORY_LIMIT_MB=" + memoryLimitMb)
                    .exec();

            containerId = container.getId();

            // Run
            long startTime = System.currentTimeMillis();
            dockerClient.startContainerCmd(containerId).exec();

            boolean finished = dockerClient.waitContainerCmd(containerId)
                    .exec(new WaitContainerResultCallback())
                    .awaitCompletion(overallTimeoutSec, TimeUnit.SECONDS);

            long totalTime = System.currentTimeMillis() - startTime;

            if (!finished) {
                try { dockerClient.killContainerCmd(containerId).exec(); }
                catch (Exception e) { log.warn("Failed to kill timed-out container {}", containerId, e); }
            }

            // Check OOM
            var inspectResponse = dockerClient.inspectContainerCmd(containerId).exec();
            Boolean oomKilled = inspectResponse.getState().getOOMKilled();
            if (Boolean.TRUE.equals(oomKilled)) {
                return new SandboxResult(false, null, List.of(), totalTime, true);
            }

            // Check compile error
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

            // Read test results
            List<SandboxResult.TestCaseResult> results = new ArrayList<>();
            for (int i = 0; i < testInputs.size(); i++) {
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

            return new SandboxResult(false, null, results, totalTime, false);

        } catch (Exception e) {
            log.error("Sandbox execution failed", e);
            return new SandboxResult(false, "Sandbox error: " + e.getMessage(),
                    List.of(), 0, false);
        } finally {
            if (containerId != null) {
                try { dockerClient.removeContainerCmd(containerId).withForce(true).exec(); }
                catch (Exception e) { log.warn("Failed to remove container {}", containerId, e); }
            }
            if (tempDir != null) {
                deleteRecursive(tempDir);
            }
        }
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
        } catch (Exception e) { log.warn("Failed to clean up temp directory {}", dir, e); }
    }
}
