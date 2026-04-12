package com.deadlock.service;

import com.deadlock.model.Problem;
import com.deadlock.repository.ProblemRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProblemSeedService {

    private final ProblemRepository problemRepository;
    private final StorageService storageService;
    private final ObjectMapper objectMapper;

    @EventListener(ApplicationReadyEvent.class)
    public void seedProblemsIfEmpty() {
        if (problemRepository.count() > 0) {
            log.info("Problems already seeded, skipping");
            return;
        }

        try {
            seedFromClasspath();
        } catch (IOException e) {
            log.error("Failed to seed problems", e);
        }
    }

    private void seedFromClasspath() throws IOException {
        Resource jsonResource = new ClassPathResource("seed/problems.json");
        if (!jsonResource.exists()) {
            log.warn("No seed/problems.json found, skipping seed");
            return;
        }

        JsonNode problems = objectMapper.readTree(jsonResource.getInputStream());
        log.info("Seeding {} problems...", problems.size());

        for (JsonNode node : problems) {
            String slug = node.get("slug").asText();
            log.info("Seeding problem: {}", slug);

            Problem problem = new Problem();
            problem.setTitle(node.get("title").asText());
            problem.setSlug(slug);
            problem.setDescription(node.get("description").asText());
            problem.setInputFormat(node.get("inputFormat").asText());
            problem.setOutputFormat(node.get("outputFormat").asText());
            problem.setConstraints(node.get("constraints").asText());
            problem.setRating(node.get("rating").asInt());
            problem.setTimeLimitMs(node.has("timeLimitMs") ? node.get("timeLimitMs").asInt() : 2000);
            problem.setMemoryLimitMb(node.has("memoryLimitMb") ? node.get("memoryLimitMb").asInt() : 256);
            problem.setSampleCount(node.get("sampleCount").asInt());

            if (node.has("functionName")) {
                problem.setFunctionName(node.get("functionName").asText());
            }
            if (node.has("params")) {
                problem.setParams(objectMapper.writeValueAsString(node.get("params")));
            }
            if (node.has("returnType")) {
                problem.setReturnType(node.get("returnType").asText());
            }

            Problem saved = problemRepository.save(problem);

            // Upload test case files to S3
            int testIndex = 0;
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            String pattern = "classpath:seed/problems/" + slug + "/tests/*-input.txt";
            try {
                Resource[] inputFiles = resolver.getResources(pattern);
                for (Resource inputFile : inputFiles) {
                    String filename = inputFile.getFilename();
                    String idx = filename.replace("-input.txt", "");
                    String outputFilename = idx + "-output.txt";

                    byte[] inputData = inputFile.getInputStream().readAllBytes();
                    Resource outputResource = new ClassPathResource(
                            "seed/problems/" + slug + "/tests/" + outputFilename);
                    byte[] outputData = outputResource.getInputStream().readAllBytes();

                    String prefix = saved.getId() + "/tests/";
                    storageService.uploadFile(prefix + idx + "-input.txt", inputData, "text/plain");
                    storageService.uploadFile(prefix + idx + "-output.txt", outputData, "text/plain");
                    testIndex++;
                }
            } catch (IOException e) {
                log.warn("No test files found for problem {}: {}", slug, e.getMessage());
            }

            saved.setTestCaseCount(testIndex);
            problemRepository.save(saved);
        }

        log.info("Problem seeding complete");
    }
}
