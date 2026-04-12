package com.deadlock.dto;

import com.deadlock.model.Submission;

public record SubmissionResponse(
        Long id, String problemSlug, String language, String status, String verdict,
        Integer failedTestCase, Integer executionTimeMs, String submittedAt
) {
    public static SubmissionResponse from(Submission s) {
        return new SubmissionResponse(s.getId(), s.getProblem().getSlug(), s.getLanguage(),
                s.getStatus(), s.getVerdict(), s.getFailedTestCase(),
                s.getExecutionTimeMs(), s.getSubmittedAt().toString());
    }
}
