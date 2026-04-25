package com.deadlock.match;

import com.deadlock.exception.InvalidInputException;
import com.deadlock.exception.ResourceNotFoundException;
import com.deadlock.model.*;
import com.deadlock.repository.MatchRepository;
import com.deadlock.repository.ProblemRepository;
import com.deadlock.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchmakingService {

    private final MatchmakingQueue queue;
    private final MatchRepository matchRepository;
    private final UserRepository userRepository;
    private final ProblemRepository problemRepository;
    private final MatchEventPublisher eventPublisher;

    private static final Random RANDOM = new Random();

    /**
     * Add a user to the matchmaking queue.
     * @throws InvalidInputException if user is already in queue or has an active match
     */
    public void joinQueue(Long userId, TimeControl timeControl, DifficultyPreference difficulty) {
        if (queue.contains(userId)) {
            throw new InvalidInputException("Already in queue");
        }
        if (matchRepository.findActiveByUserId(userId).isPresent()) {
            throw new InvalidInputException("Already in an active match");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));
        if (user.getUsername() == null) {
            throw new InvalidInputException("Set a username before queueing");
        }

        QueueEntry entry = new QueueEntry(userId, user.getEloRating(), timeControl, difficulty, Instant.now());
        queue.add(entry);
        log.info("User {} joined queue ({}, {}, ELO {})", userId, timeControl, difficulty, user.getEloRating());
    }

    public void leaveQueue(Long userId) {
        queue.remove(userId).ifPresent(e -> log.info("User {} left queue", userId));
    }

    public boolean isInQueue(Long userId) {
        return queue.contains(userId);
    }

    public int queueSize() {
        return queue.size();
    }

    /**
     * Runs every 2 seconds. Pairs the longest-waiting players whose ranges overlap.
     */
    @Scheduled(fixedDelay = 2000)
    @Transactional
    public void attemptMatchmaking() {
        List<QueueEntry> candidates = queue.snapshotByJoinTime();
        if (candidates.size() < 2) return;

        Set<Long> alreadyPaired = new HashSet<>();

        for (int i = 0; i < candidates.size(); i++) {
            QueueEntry a = candidates.get(i);
            if (alreadyPaired.contains(a.userId())) continue;

            for (int j = i + 1; j < candidates.size(); j++) {
                QueueEntry b = candidates.get(j);
                if (alreadyPaired.contains(b.userId())) continue;

                if (a.canMatchWith(b)) {
                    if (createMatchSafely(a, b)) {
                        alreadyPaired.add(a.userId());
                        alreadyPaired.add(b.userId());
                    }
                    break;
                }
            }
        }
    }

    private boolean createMatchSafely(QueueEntry a, QueueEntry b) {
        // Remove from queue first to avoid double-pairing
        if (queue.remove(a.userId()).isEmpty() || queue.remove(b.userId()).isEmpty()) {
            // One left already; put the other back
            queue.add(a);
            queue.add(b);
            return false;
        }

        try {
            createMatch(a, b);
            return true;
        } catch (RuntimeException e) {
            log.error("Failed to create match for {} and {}", a.userId(), b.userId(), e);
            queue.add(a);
            queue.add(b);
            return false;
        }
    }

    private void createMatch(QueueEntry a, QueueEntry b) {
        User user1 = userRepository.findById(a.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User", a.userId().toString()));
        User user2 = userRepository.findById(b.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User", b.userId().toString()));

        Problem problem = pickProblem(a, b);

        Match match = new Match();
        match.setPlayer1(user1);
        match.setPlayer2(user2);
        match.setProblem(problem);
        match.setStatus(MatchStatus.IN_PROGRESS);  // Skip WAITING; clients connect when they navigate
        match.setDurationSeconds(a.timeControl().getDurationSeconds());
        match.setPlayer1EloBefore(user1.getEloRating());
        match.setPlayer2EloBefore(user2.getEloRating());

        Match saved = matchRepository.save(match);
        log.info("Created match {} between {} and {} on problem {}",
                saved.getId(), user1.getUsername(), user2.getUsername(), problem.getSlug());

        eventPublisher.publishMatchFound(user1.getId(),
                MatchEventPayloads.matchFound(saved, user2, problem));
        eventPublisher.publishMatchFound(user2.getId(),
                MatchEventPayloads.matchFound(saved, user1, problem));
    }

    private Problem pickProblem(QueueEntry a, QueueEntry b) {
        DifficultyPreference effective;
        if (a.difficulty() == DifficultyPreference.ANY && b.difficulty() == DifficultyPreference.ANY) {
            // Use average ELO to pick something appropriate
            int avgElo = (a.eloRating() + b.eloRating()) / 2;
            effective = inferDifficulty(avgElo);
        } else if (a.difficulty() == DifficultyPreference.ANY) {
            effective = b.difficulty();
        } else {
            effective = a.difficulty();
        }

        var page = problemRepository.searchByRatingAndTitle(
                effective.getMinRating(),
                effective.getMaxRating(),
                "",
                org.springframework.data.domain.PageRequest.of(0, 100));
        var problems = page.getContent();
        if (problems.isEmpty()) {
            // Fallback: any problem
            problems = problemRepository.findAll();
        }
        return problems.get(RANDOM.nextInt(problems.size()));
    }

    private DifficultyPreference inferDifficulty(int avgElo) {
        if (avgElo <= 1000) return DifficultyPreference.BEGINNER;
        if (avgElo <= 1400) return DifficultyPreference.EASY;
        if (avgElo <= 1800) return DifficultyPreference.MEDIUM;
        if (avgElo <= 2200) return DifficultyPreference.HARD;
        return DifficultyPreference.EXPERT;
    }
}
