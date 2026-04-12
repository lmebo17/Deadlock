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

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class DockerSandboxService implements SandboxService {

    private final DockerClient dockerClient;
    private final SandboxFileManager fileManager;

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

        Path tempDir = null;
        String containerId = null;
        try {
            // Set up temp directory with code and test files
            tempDir = fileManager.createSandboxDir(code, language, testInputs);

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
            SandboxResult compileError = fileManager.readCompileError(tempDir, totalTime);
            if (compileError != null) {
                return compileError;
            }

            // Read test results
            List<SandboxResult.TestCaseResult> results = fileManager.readResults(tempDir, testInputs.size());
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
                fileManager.cleanup(tempDir);
            }
        }
    }
}
