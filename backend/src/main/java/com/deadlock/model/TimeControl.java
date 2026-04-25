package com.deadlock.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TimeControl {
    BLITZ(300),
    RAPID(900),
    CLASSICAL(1800);

    private final int durationSeconds;
}
