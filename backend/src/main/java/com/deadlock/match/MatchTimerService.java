package com.deadlock.match;

import com.deadlock.model.Match;
import com.deadlock.model.MatchStatus;
import com.deadlock.repository.MatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchTimerService {

    private final MatchRepository matchRepository;
    private final MatchService matchService;

    /**
     * On startup, mark any IN_PROGRESS matches as cancelled (server restarted mid-match).
     */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void cleanupStaleMatchesOnStartup() {
        var stale = matchRepository.findByStatus(MatchStatus.IN_PROGRESS);
        for (Match m : stale) {
            log.warn("Cancelling stale IN_PROGRESS match {} on startup", m.getId());
            m.setStatus(MatchStatus.CANCELLED);
            m.setEndedAt(Instant.now());
            matchRepository.save(m);
        }
        var waiting = matchRepository.findByStatus(MatchStatus.WAITING);
        for (Match m : waiting) {
            log.warn("Cancelling stale WAITING match {} on startup", m.getId());
            m.setStatus(MatchStatus.CANCELLED);
            m.setEndedAt(Instant.now());
            matchRepository.save(m);
        }
    }

    /**
     * Every 5 seconds, find expired matches and finish them as draws.
     */
    @Scheduled(fixedDelay = 5000)
    public void expireFinishedMatches() {
        var inProgress = matchRepository.findByStatus(MatchStatus.IN_PROGRESS);
        Instant now = Instant.now();
        for (Match m : inProgress) {
            Instant deadline = m.getStartedAt().plusSeconds(m.getDurationSeconds());
            if (now.isAfter(deadline)) {
                log.info("Match {} timer expired, finishing as draw", m.getId());
                matchService.finishMatch(m.getId(), null);
            }
        }
    }
}
