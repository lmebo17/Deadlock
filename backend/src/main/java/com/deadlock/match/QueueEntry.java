package com.deadlock.match;

import com.deadlock.model.DifficultyPreference;
import com.deadlock.model.TimeControl;

import java.time.Instant;

public record QueueEntry(
        Long userId,
        int eloRating,
        TimeControl timeControl,
        DifficultyPreference difficulty,
        Instant joinedAt
) {
    public int waitTimeSec() {
        return (int) (Instant.now().getEpochSecond() - joinedAt.getEpochSecond());
    }

    /**
     * ELO tolerance grows with wait time. Hard cap of 800.
     * 0-30s: 200, 30-60s: 400, 60-90s: 600, 90s+: 800
     */
    public int currentEloTolerance() {
        int wait = waitTimeSec();
        if (wait < 30) return 200;
        if (wait < 60) return 400;
        if (wait < 90) return 600;
        return 800;
    }

    public boolean canMatchWith(QueueEntry other) {
        if (this.timeControl != other.timeControl) return false;
        if (!this.difficulty.compatibleWith(other.difficulty)) return false;
        int gap = Math.abs(this.eloRating - other.eloRating);
        return gap <= Math.min(this.currentEloTolerance(), other.currentEloTolerance());
    }
}
