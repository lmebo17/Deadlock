package com.deadlock.service;

import com.deadlock.dto.SubmissionResponse;

import java.util.List;

public interface SubmissionService {
    Long submit(Long userId, String problemSlug, String language, String code);
    SubmissionResponse getSubmission(Long id);
    List<SubmissionResponse> getUserSubmissionsForProblem(Long userId, String problemSlug);
}
