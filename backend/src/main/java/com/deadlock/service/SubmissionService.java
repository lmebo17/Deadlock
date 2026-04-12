package com.deadlock.service;

import com.deadlock.dto.SubmissionResponse;

public interface SubmissionService {
    Long submit(Long userId, String problemSlug, String language, String code);
    SubmissionResponse getSubmission(Long id);
}
