package com.deadlock.dto;

import com.deadlock.model.Submission;

public record SubmissionResponse(
        Long id, Long userId, String username, String problemSlug, String language,
        String status, String verdict, Integer failedTestCase, Integer executionTimeMs,
        String code, String submittedAt
) {
    public static SubmissionResponse from(Submission s) {
        return new SubmissionResponse(
                s.getId(),
                s.getUser() != null ? s.getUser().getId() : null,
                s.getUser() != null ? s.getUser().getUsername() : null,
                s.getProblem().getSlug(),
                s.getLanguage().name(),
                s.getStatus().name(),
                s.getVerdict() != null ? s.getVerdict().name() : null,
                s.getFailedTestCase(),
                s.getExecutionTimeMs(),
                s.getCode(),
                s.getSubmittedAt().toString()
        );
    }
}
