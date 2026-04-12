package com.deadlock.dto;

import com.deadlock.model.Problem;
import java.util.List;

public record ProblemDetailResponse(
        Long id, String title, String slug, String description,
        String inputFormat, String outputFormat, String constraints,
        int rating, String tierLabel, int timeLimitMs, int memoryLimitMb,
        int testCaseCount, int sampleCount, List<TestCaseResponse> sampleTestCases,
        String functionName, String returnType
) {
    public static ProblemDetailResponse from(Problem p, List<TestCaseResponse> samples) {
        return new ProblemDetailResponse(p.getId(), p.getTitle(), p.getSlug(),
                p.getDescription(), p.getInputFormat(), p.getOutputFormat(),
                p.getConstraints(), p.getRating(), p.getTierLabel(),
                p.getTimeLimitMs(), p.getMemoryLimitMb(),
                p.getTestCaseCount(), p.getSampleCount(), samples,
                p.getFunctionName(), p.getReturnType());
    }
}
