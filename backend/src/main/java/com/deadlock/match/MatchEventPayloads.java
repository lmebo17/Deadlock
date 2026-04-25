package com.deadlock.match;

import com.deadlock.dto.MatchEvent;
import com.deadlock.dto.OpponentInfo;
import com.deadlock.model.Match;
import com.deadlock.model.Problem;
import com.deadlock.model.User;

/**
 * Helpers to build MatchEvent payloads from domain objects.
 */
public final class MatchEventPayloads {

    private MatchEventPayloads() {}

    public static MatchEvent matchFound(Match match, User opponent, Problem problem) {
        return MatchEvent.matchFound(match.getId(), OpponentInfo.from(opponent),
                problem.getSlug(), problem.getTitle());
    }

    public static MatchEvent matchStart(Match match) {
        return MatchEvent.matchStart(match.getId(), match.getDurationSeconds(),
                match.getStartedAt().toString());
    }

    public static MatchEvent opponentSubmitted(Long matchId, int submissionCount) {
        return MatchEvent.opponentSubmitted(matchId, submissionCount);
    }

    public static MatchEvent matchEnd(Match match, Long perspectiveUserId, int yourFinalElo) {
        boolean isPlayer1 = match.getPlayer1().getId().equals(perspectiveUserId);
        Integer yourChange = isPlayer1 ? match.getPlayer1EloChange() : match.getPlayer2EloChange();
        Integer oppChange = isPlayer1 ? match.getPlayer2EloChange() : match.getPlayer1EloChange();

        return MatchEvent.matchEnd(
                match.getId(),
                match.getWinner() != null ? match.getWinner().getId() : null,
                yourChange != null ? yourChange : 0,
                oppChange != null ? oppChange : 0,
                yourFinalElo,
                match.getStatus().name()
        );
    }
}
