package com.deadlock.repository;

import com.deadlock.model.Problem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProblemRepository extends JpaRepository<Problem, Long> {

    Optional<Problem> findBySlug(String slug);

    Page<Problem> findByRatingBetween(int minRating, int maxRating, Pageable pageable);

    @Query("SELECT p FROM Problem p WHERE p.rating BETWEEN :min AND :max AND LOWER(p.title) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Problem> searchByRatingAndTitle(@Param("min") int min, @Param("max") int max,
                                          @Param("search") String search, Pageable pageable);
}
