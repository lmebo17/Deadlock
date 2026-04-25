package com.deadlock.match;

import com.deadlock.dto.MatchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledFuture;

/**
 * Tracks WebSocket connection state per user and forfeits an active match
 * if a user stays disconnected longer than the grace period.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MatchPresenceService {

    private static final Duration GRACE_PERIOD = Duration.ofSeconds(30);

    private final MatchService matchService;
    private final MatchEventPublisher eventPublisher;
    private final TaskScheduler taskScheduler;

    /** userId -> active session count (a user may have multiple tabs open). */
    private final ConcurrentMap<Long, Integer> sessionCounts = new ConcurrentHashMap<>();

    /** userId -> pending forfeit task (so we can cancel on reconnect). */
    private final ConcurrentMap<Long, ScheduledFuture<?>> pendingForfeits = new ConcurrentHashMap<>();

    @EventListener
    public void onConnected(SessionConnectedEvent event) {
        Long userId = userIdFrom(event.getUser());
        if (userId == null) return;

        int newCount = sessionCounts.merge(userId, 1, Integer::sum);
        log.debug("WS connected: user {} (sessions={})", userId, newCount);

        // Cancel any pending forfeit for this user
        ScheduledFuture<?> pending = pendingForfeits.remove(userId);
        if (pending != null && !pending.isDone()) {
            pending.cancel(false);
            log.info("Forfeit cancelled for user {} — reconnected within grace", userId);
            notifyOpponentReconnected(userId);
        }
    }

    @EventListener
    public void onDisconnected(SessionDisconnectEvent event) {
        Long userId = userIdFrom(event.getUser());
        if (userId == null) return;

        // Decrement; remove entry when it reaches 0
        Integer remaining = sessionCounts.computeIfPresent(userId,
                (k, v) -> v <= 1 ? null : v - 1);

        log.debug("WS disconnected: user {} (sessions={})", userId, remaining == null ? 0 : remaining);

        if (remaining != null && remaining > 0) return; // still has another tab open

        // Last session for this user closed — schedule forfeit if they're in an active match
        Optional<MatchResponse> activeOpt = matchService.getActiveMatch(userId);
        if (activeOpt.isEmpty()) return;
        MatchResponse active = activeOpt.get();
        Long matchId = active.id();
        Long opponentId = active.player1().id().equals(userId)
                ? active.player2().id()
                : active.player1().id();

        eventPublisher.publishToMatch(matchId,
                com.deadlock.dto.MatchEvent.opponentDisconnected(matchId));

        ScheduledFuture<?> task = taskScheduler.schedule(
                () -> forfeitIfStillDisconnected(userId, matchId, opponentId),
                Instant.now().plus(GRACE_PERIOD)
        );
        pendingForfeits.put(userId, task);
        log.info("Scheduled forfeit for user {} in match {} in {}s", userId, matchId, GRACE_PERIOD.toSeconds());
    }

    private void forfeitIfStillDisconnected(Long userId, Long matchId, Long winnerId) {
        if (sessionCounts.getOrDefault(userId, 0) > 0) {
            log.debug("Forfeit task fired but user {} reconnected — skipping", userId);
            return;
        }
        // Re-check match still active
        Optional<MatchResponse> active = matchService.getActiveMatch(userId);
        if (active.isEmpty() || !active.get().id().equals(matchId)) {
            log.debug("Match {} no longer active for user {} — skipping forfeit", matchId, userId);
            pendingForfeits.remove(userId);
            return;
        }
        log.info("User {} stayed disconnected past grace — forfeiting match {} to user {}",
                userId, matchId, winnerId);
        try {
            matchService.finishMatch(matchId, winnerId);
        } catch (Exception e) {
            log.error("Failed to forfeit match {}: {}", matchId, e.getMessage(), e);
        } finally {
            pendingForfeits.remove(userId);
        }
    }

    private void notifyOpponentReconnected(Long userId) {
        // Only notify if they're still in an active match
        matchService.getActiveMatch(userId).ifPresent(m -> eventPublisher.publishToMatch(m.id(),
                com.deadlock.dto.MatchEvent.opponentReconnected(m.id())));
    }

    private Long userIdFrom(Principal principal) {
        if (principal == null) return null;
        try {
            return Long.parseLong(principal.getName());
        } catch (NumberFormatException e) {
            log.warn("Cannot parse userId from principal name '{}'", principal.getName());
            return null;
        }
    }
}
