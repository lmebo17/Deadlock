package com.deadlock.dto;

import com.deadlock.model.Problem;

public record ProblemResponse(
        Long id, String title, String slug, int rating, String tierLabel,
        int timeLimitMs, int memoryLimitMb, int testCaseCount, int sampleCount
) {
    public static ProblemResponse from(Problem p) {
        return new ProblemResponse(p.getId(), p.getTitle(), p.getSlug(), p.getRating(),
                p.getTierLabel(), p.getTimeLimitMs(), p.getMemoryLimitMb(),
                p.getTestCaseCount(), p.getSampleCount());
    }
}
