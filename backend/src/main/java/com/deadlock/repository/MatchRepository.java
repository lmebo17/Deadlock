package com.deadlock.repository;

import com.deadlock.model.Match;
import com.deadlock.model.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MatchRepository extends JpaRepository<Match, Long> {

    @Query("""
        SELECT m FROM Match m
        WHERE (m.player1.id = :userId OR m.player2.id = :userId)
          AND m.status IN (com.deadlock.model.MatchStatus.WAITING, com.deadlock.model.MatchStatus.IN_PROGRESS)
    """)
    Optional<Match> findActiveByUserId(@Param("userId") Long userId);

    @Query("""
        SELECT m FROM Match m
        WHERE m.player1.username = :username OR m.player2.username = :username
        ORDER BY m.startedAt DESC
    """)
    List<Match> findByUsernameOrderByStartedAtDesc(@Param("username") String username);

    List<Match> findByStatus(MatchStatus status);

    /**
     * Atomic claim of winner. Returns 1 if this update set the winner, 0 if someone else already won.
     */
    @Modifying
    @Query("""
        UPDATE Match m
        SET m.winner.id = :winnerId,
            m.status = com.deadlock.model.MatchStatus.FINISHED,
            m.endedAt = :now
        WHERE m.id = :matchId
          AND m.winner IS NULL
          AND m.status = com.deadlock.model.MatchStatus.IN_PROGRESS
    """)
    int claimWinner(@Param("matchId") Long matchId,
                    @Param("winnerId") Long winnerId,
                    @Param("now") java.time.Instant now);
}
