package com.deadlock.match;

import com.deadlock.dto.MatchResponse;
import com.deadlock.exception.ResourceNotFoundException;

import java.util.List;
import java.util.Optional;

public interface MatchService {

    MatchResponse getMatch(Long matchId);

    Optional<MatchResponse> getActiveMatch(Long userId);

    List<MatchResponse> getMatchHistory(String username);

    /**
     * Called when a submission for an active match gets verdict ACCEPTED.
     * Atomically marks this user as the winner. If someone else already won, no-op.
     * @return true if this call set the winner (i.e. this was the winning submission)
     */
    boolean tryResolveWinner(Long matchId, Long winnerUserId);

    /**
     * Mark a match as a draw (timer expired with no winner) or a forfeit.
     * @param winnerUserId optional winner (e.g. on disconnect forfeit), null for draw
     */
    void finishMatch(Long matchId, Long winnerUserId);
}
