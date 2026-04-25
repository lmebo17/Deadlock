package com.deadlock.match;

import com.deadlock.dto.MatchEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MatchEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    /** Send to a specific user (for MATCH_FOUND, MATCH_END). */
    public void publishToUser(Long userId, MatchEvent event) {
        // Spring's user destination prefix. The user is identified by Principal.getName()
        // which we set to the userId (string form) in the WebSocket auth interceptor.
        String userIdStr = userId.toString();
        messagingTemplate.convertAndSendToUser(userIdStr, "/queue/match-events", event);
        log.debug("Sent {} to user {}", event.type(), userId);
    }

    /** Send to everyone subscribed to this match (for OPPONENT_SUBMITTED, etc.). */
    public void publishToMatch(Long matchId, MatchEvent event) {
        messagingTemplate.convertAndSend("/topic/match/" + matchId, event);
        log.debug("Sent {} to match {}", event.type(), matchId);
    }

    public void publishMatchFound(Long userId, MatchEvent event) {
        publishToUser(userId, event);
    }
}
