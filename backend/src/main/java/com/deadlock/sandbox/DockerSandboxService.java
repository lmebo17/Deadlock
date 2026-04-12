package com.deadlock.sandbox;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.WaitContainerResultCallback;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Capability;
import com.github.dockerjava.api.model.HostConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class DockerSandboxService implements SandboxService {

    private static final Map<String, String> LANGUAGE_IMAGES = Map.of(
            "JAVA", "deadlock-sandbox-java",
            "PYTHON", "deadlock-sandbox-python",
            "CPP", "deadlock-sandbox-cpp"
    );

    private static final Map<String, String> LANGUAGE_EXTENSIONS = Map.of(
            "JAVA", "java",
            "PYTHON", "py",
            "CPP", "cpp"
    );

    private final DockerClient dockerClient;

    public DockerSandboxService(DockerClient dockerClient) {
        this.dockerClient = dockerClient;
    }

    @Override
    public SandboxResult execute(String code, String language, String input,
                                  int timeLimitMs, int memoryLimitMb) {
        String image = LANGUAGE_IMAGES.get(language.toUpperCase());
        String ext = LANGUAGE_EXTENSIONS.get(language.toUpperCase());
        if (image == null || ext == null) {
            return new SandboxResult(1, "", "Unsupported language: " + language, 0, false, false);
        }

        Path tempDir = null;
        String containerId = null;
        try {
            tempDir = Files.createTempDirectory("deadlock-sandbox-");
            Files.writeString(tempDir.resolve("solution." + ext), code);
            Files.writeString(tempDir.resolve("input.txt"), input);
            Files.writeString(tempDir.resolve("output.txt"), "");
            Files.writeString(tempDir.resolve("compile_error.txt"), "");
            Files.writeString(tempDir.resolve("runtime_error.txt"), "");

            // Make files accessible by sandbox user (uid 1000)
            tempDir.toFile().setReadable(true, false);
            tempDir.toFile().setWritable(true, false);
            tempDir.toFile().setExecutable(true, false);
            for (var f : tempDir.toFile().listFiles()) {
                f.setReadable(true, false);
                f.setWritable(true, false);
            }

            int timeLimitSec = Math.max(1, timeLimitMs / 1000);

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

            long startTime = System.currentTimeMillis();
            dockerClient.startContainerCmd(containerId).exec();

            int overallTimeoutSec = Math.min(timeLimitSec + 5, 120);
            boolean finished = dockerClient.waitContainerCmd(containerId)
                    .exec(new WaitContainerResultCallback())
                    .awaitCompletion(overallTimeoutSec, TimeUnit.SECONDS);

            long executionTime = System.currentTimeMillis() - startTime;

            if (!finished) {
                try { dockerClient.killContainerCmd(containerId).exec(); } catch (Exception ignored) {}
                return new SandboxResult(124, "", "Time limit exceeded", executionTime, true, false);
            }

            var inspectResponse = dockerClient.inspectContainerCmd(containerId).exec();
            Boolean oomKilled = inspectResponse.getState().getOOMKilled();
            int exitCode = inspectResponse.getState().getExitCode();

            if (Boolean.TRUE.equals(oomKilled)) {
                return new SandboxResult(137, "", "Memory limit exceeded", executionTime, false, true);
            }

            String stdout = Files.readString(tempDir.resolve("output.txt"), StandardCharsets.UTF_8);
            String stderr = "";
            if (exitCode == 2) {
                stderr = Files.readString(tempDir.resolve("compile_error.txt"), StandardCharsets.UTF_8);
            } else if (exitCode != 0) {
                stderr = Files.readString(tempDir.resolve("runtime_error.txt"), StandardCharsets.UTF_8);
            }

            return new SandboxResult(exitCode, stdout, stderr, executionTime, false, false);

        } catch (Exception e) {
            log.error("Sandbox execution failed", e);
            return new SandboxResult(1, "", "Sandbox error: " + e.getMessage(), 0, false, false);
        } finally {
            if (containerId != null) {
                try { dockerClient.removeContainerCmd(containerId).withForce(true).exec(); }
                catch (Exception ignored) {}
            }
            if (tempDir != null) {
                try {
                    for (var f : tempDir.toFile().listFiles()) f.delete();
                    tempDir.toFile().delete();
                } catch (Exception ignored) {}
            }
        }
    }
}
