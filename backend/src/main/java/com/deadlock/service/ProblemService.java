package com.deadlock.service;

import com.deadlock.dto.CreateProblemRequest;
import com.deadlock.dto.ProblemDetailResponse;
import com.deadlock.dto.ProblemResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProblemService {

    Page<ProblemResponse> listProblems(int minRating, int maxRating, String search, Pageable pageable);

    ProblemDetailResponse getProblemBySlug(String slug);

    ProblemResponse createProblem(CreateProblemRequest req);

    void uploadTestCases(Long problemId, List<byte[]> inputs, List<byte[]> outputs, int sampleCount);
}
