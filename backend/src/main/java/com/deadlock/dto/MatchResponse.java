package com.deadlock.dto;

import com.deadlock.model.Match;

public record MatchResponse(
        Long id,
        OpponentInfo player1,
        OpponentInfo player2,
        String problemSlug,
        String problemTitle,
        Long winnerId,
        String status,
        int durationSeconds,
        Integer player1EloChange,
        Integer player2EloChange,
        String startedAt,
        String endedAt
) {
    public static MatchResponse from(Match match) {
        return new MatchResponse(
                match.getId(),
                OpponentInfo.from(match.getPlayer1()),
                OpponentInfo.from(match.getPlayer2()),
                match.getProblem().getSlug(),
                match.getProblem().getTitle(),
                match.getWinner() != null ? match.getWinner().getId() : null,
                match.getStatus().name(),
                match.getDurationSeconds(),
                match.getPlayer1EloChange(),
                match.getPlayer2EloChange(),
                match.getStartedAt().toString(),
                match.getEndedAt() != null ? match.getEndedAt().toString() : null
        );
    }
}
