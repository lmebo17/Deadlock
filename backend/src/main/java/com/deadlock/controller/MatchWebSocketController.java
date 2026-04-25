package com.deadlock.controller;

import com.deadlock.dto.MatchEvent;
import com.deadlock.dto.QueueJoinRequest;
import com.deadlock.match.MatchEventPublisher;
import com.deadlock.match.MatchmakingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Slf4j
@Controller
@RequiredArgsConstructor
public class MatchWebSocketController {

    private final MatchmakingService matchmakingService;
    private final MatchEventPublisher eventPublisher;

    /**
     * Client sends to `/app/queue/join` to join matchmaking.
     */
    @MessageMapping("/queue/join")
    public void joinQueue(@Payload QueueJoinRequest request, Principal principal) {
        if (principal == null) {
            log.warn("Queue join rejected: no principal");
            return;
        }
        Long userId = Long.parseLong(principal.getName());
        try {
            matchmakingService.joinQueue(userId, request.timeControl(), request.difficulty());
            // Send a confirmation back
            eventPublisher.publishToUser(userId, MatchEvent.queueUpdate(
                    matchmakingService.queueSize(), 0));
        } catch (RuntimeException e) {
            log.warn("Queue join failed for user {}: {}", userId, e.getMessage());
        }
    }

    /**
     * Client sends to `/app/queue/leave` to leave matchmaking.
     */
    @MessageMapping("/queue/leave")
    public void leaveQueue(Principal principal) {
        if (principal == null) return;
        Long userId = Long.parseLong(principal.getName());
        matchmakingService.leaveQueue(userId);
    }
}
