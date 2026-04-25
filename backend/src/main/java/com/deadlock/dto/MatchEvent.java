package com.deadlock.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MatchEvent(
        String type,
        Long matchId,
        Long opponentId,
        String opponentUsername,
        String opponentAvatarUrl,
        Integer opponentElo,
        String problemSlug,
        String problemTitle,
        Integer durationSec,
        String startedAt,
        Integer submissionCount,
        Long winnerId,
        Integer yourEloChange,
        Integer opponentEloChange,
        Integer yourFinalElo,
        String finalStatus,
        Integer queueSize,
        Integer waitTimeSec
) {

    public static MatchEvent queueUpdate(int queueSize, int waitTimeSec) {
        return new MatchEvent("QUEUE_UPDATE", null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, queueSize, waitTimeSec);
    }

    public static MatchEvent matchFound(Long matchId, OpponentInfo opponent, String problemSlug, String problemTitle) {
        return new MatchEvent("MATCH_FOUND", matchId, opponent.id(), opponent.username(),
                opponent.avatarUrl(), opponent.eloRating(), problemSlug, problemTitle, null,
                null, null, null, null, null, null, null, null, null);
    }

    public static MatchEvent matchStart(Long matchId, int durationSec, String startedAt) {
        return new MatchEvent("MATCH_START", matchId, null, null, null, null, null, null,
                durationSec, startedAt, null, null, null, null, null, null, null, null);
    }

    public static MatchEvent opponentSubmitted(Long matchId, int submissionCount) {
        return new MatchEvent("OPPONENT_SUBMITTED", matchId, null, null, null, null, null, null,
                null, null, submissionCount, null, null, null, null, null, null, null);
    }

    public static MatchEvent opponentDisconnected(Long matchId) {
        return new MatchEvent("OPPONENT_DISCONNECTED", matchId, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null);
    }

    public static MatchEvent opponentReconnected(Long matchId) {
        return new MatchEvent("OPPONENT_RECONNECTED", matchId, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null);
    }

    public static MatchEvent matchEnd(Long matchId, Long winnerId, int yourChange, int opponentChange,
                                       int yourFinalElo, String finalStatus) {
        return new MatchEvent("MATCH_END", matchId, null, null, null, null, null, null, null,
                null, null, winnerId, yourChange, opponentChange, yourFinalElo, finalStatus, null, null);
    }
}
