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
     * flushAutomatically + clearAutomatically ensure subsequent findById sees the fresh state
     * (no stale L1 cache).
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
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

    /**
     * Aggregate match stats for a user across finished or cancelled matches.
     */
    interface UserMatchStats {
        Long getWins();
        Long getLosses();
        Long getDraws();
    }

    @Query("""
        SELECT
            COALESCE(SUM(CASE WHEN m.winner.id = :userId THEN 1 ELSE 0 END), 0) AS wins,
            COALESCE(SUM(CASE WHEN m.winner.id IS NOT NULL AND m.winner.id <> :userId THEN 1 ELSE 0 END), 0) AS losses,
            COALESCE(SUM(CASE WHEN m.winner.id IS NULL AND m.status = com.deadlock.model.MatchStatus.FINISHED THEN 1 ELSE 0 END), 0) AS draws
        FROM Match m
        WHERE (m.player1.id = :userId OR m.player2.id = :userId)
          AND m.status IN (com.deadlock.model.MatchStatus.FINISHED, com.deadlock.model.MatchStatus.CANCELLED)
    """)
    UserMatchStats aggregateStats(@Param("userId") Long userId);

    @Query("""
        SELECT m FROM Match m
        WHERE (m.player1.id = :userId OR m.player2.id = :userId)
          AND m.status IN (com.deadlock.model.MatchStatus.FINISHED, com.deadlock.model.MatchStatus.CANCELLED)
        ORDER BY m.endedAt ASC
    """)
    List<Match> findFinishedByUserIdAsc(@Param("userId") Long userId);
}
