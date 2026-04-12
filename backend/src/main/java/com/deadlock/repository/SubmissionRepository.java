package com.deadlock.repository;

import com.deadlock.model.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findByUserIdAndProblemIdOrderBySubmittedAtDesc(Long userId, Long problemId);
    List<Submission> findByUserIdOrderBySubmittedAtDesc(Long userId);
}
