package com.deadlock.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class SubmissionJudgedEvent extends ApplicationEvent {
    private final Long submissionId;
    private final String verdict;

    public SubmissionJudgedEvent(Object source, Long submissionId, String verdict) {
        super(source);
        this.submissionId = submissionId;
        this.verdict = verdict;
    }
}
