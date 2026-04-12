package com.deadlock.service;

import com.deadlock.dto.*;
import com.deadlock.exception.ResourceNotFoundException;
import com.deadlock.model.Problem;
import com.deadlock.repository.ProblemRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProblemServiceImpl implements ProblemService {

    private final ProblemRepository problemRepository;
    private final StorageService storageService;

    public ProblemServiceImpl(ProblemRepository problemRepository, StorageService storageService) {
        this.problemRepository = problemRepository;
        this.storageService = storageService;
    }

    @Override
    public Page<ProblemResponse> listProblems(int minRating, int maxRating, String search,
                                               Pageable pageable) {
        Page<Problem> problems = problemRepository.searchByRatingAndTitle(
                minRating, maxRating, search != null ? search : "", pageable);
        return problems.map(ProblemResponse::from);
    }

    @Override
    public ProblemDetailResponse getProblemBySlug(String slug) {
        Problem problem = problemRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Problem", slug));

        List<TestCaseResponse> samples = loadSampleTestCases(problem);
        return ProblemDetailResponse.from(problem, samples);
    }

    @Override
    @Transactional
    public ProblemResponse createProblem(CreateProblemRequest req) {
        Problem problem = new Problem();
        problem.setTitle(req.title());
        problem.setSlug(req.slug());
        problem.setDescription(req.description());
        problem.setInputFormat(req.inputFormat());
        problem.setOutputFormat(req.outputFormat());
        problem.setConstraints(req.constraints());
        problem.setRating(req.rating());
        if (req.timeLimitMs() != null) problem.setTimeLimitMs(req.timeLimitMs());
        if (req.memoryLimitMb() != null) problem.setMemoryLimitMb(req.memoryLimitMb());

        Problem saved = problemRepository.save(problem);
        return ProblemResponse.from(saved);
    }

    @Override
    @Transactional
    public void uploadTestCases(Long problemId, List<byte[]> inputs, List<byte[]> outputs,
                                 int sampleCount) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new ResourceNotFoundException("Problem", problemId.toString()));

        String prefix = problem.getId() + "/tests/";

        for (int i = 0; i < inputs.size(); i++) {
            String idx = String.format("%02d", i + 1);
            storageService.uploadFile(prefix + idx + "-input.txt", inputs.get(i), "text/plain");
            storageService.uploadFile(prefix + idx + "-output.txt", outputs.get(i), "text/plain");
        }

        problem.setTestCaseCount(inputs.size());
        problem.setSampleCount(sampleCount);
        problemRepository.save(problem);
    }

    private List<TestCaseResponse> loadSampleTestCases(Problem problem) {
        List<TestCaseResponse> samples = new ArrayList<>();
        for (int i = 1; i <= problem.getSampleCount(); i++) {
            String idx = String.format("%02d", i);
            String prefix = problem.getId() + "/tests/";
            byte[] input = storageService.downloadFile(prefix + idx + "-input.txt");
            byte[] output = storageService.downloadFile(prefix + idx + "-output.txt");
            samples.add(new TestCaseResponse(i,
                    new String(input, StandardCharsets.UTF_8),
                    new String(output, StandardCharsets.UTF_8)));
        }
        return samples;
    }
}
