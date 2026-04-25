package com.deadlock.repository;

import com.deadlock.model.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findByUserIdAndProblemIdOrderBySubmittedAtDesc(Long userId, Long problemId);
    List<Submission> findByUserIdOrderBySubmittedAtDesc(Long userId);
    List<Submission> findByMatchIdOrderBySubmittedAtAsc(Long matchId);

    interface DailyCount {
        LocalDate getDay();
        Long getCount();
    }

    @Query(value = """
        SELECT CAST(s.submitted_at AT TIME ZONE 'UTC' AS DATE) AS day,
               COUNT(*) AS count
        FROM submissions s
        WHERE s.user_id = :userId
          AND s.submitted_at >= :since
        GROUP BY day
        ORDER BY day ASC
    """, nativeQuery = true)
    List<DailyCount> countDailySubmissions(@Param("userId") Long userId,
                                           @Param("since") Instant since);

    @Query("""
        SELECT DISTINCT p.slug FROM Submission s
        JOIN s.problem p
        WHERE s.user.id = :userId AND s.verdict = com.deadlock.model.Verdict.ACCEPTED
    """)
    List<String> findSolvedSlugs(@Param("userId") Long userId);

    @Query("""
        SELECT DISTINCT p.slug FROM Submission s
        JOIN s.problem p
        WHERE s.user.id = :userId
    """)
    List<String> findAttemptedSlugs(@Param("userId") Long userId);
}
