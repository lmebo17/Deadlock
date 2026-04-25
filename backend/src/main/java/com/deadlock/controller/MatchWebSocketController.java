package com.deadlock.controller;

import com.deadlock.dto.MatchEvent;
import com.deadlock.dto.QueueJoinRequest;
import com.deadlock.match.MatchEventPublisher;
import com.deadlock.match.MatchmakingService;
import com.deadlock.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Slf4j
@Controller
@RequiredArgsConstructor
public class MatchWebSocketController {

    private final MatchmakingService matchmakingService;
    private final MatchEventPublisher eventPublisher;

    @MessageMapping("/queue/join")
    public void joinQueue(@Payload QueueJoinRequest request, Principal principal) {
        Long userId = extractUserId(principal);
        if (userId == null) {
            log.warn("Queue join rejected: no authenticated user");
            return;
        }
        try {
            matchmakingService.joinQueue(userId, request.timeControl(), request.difficulty());
            eventPublisher.publishToUser(userId, MatchEvent.queueUpdate(
                    matchmakingService.queueSize(), 0));
        } catch (RuntimeException e) {
            log.warn("Queue join failed for user {}: {}", userId, e.getMessage());
        }
    }

    @MessageMapping("/queue/leave")
    public void leaveQueue(Principal principal) {
        Long userId = extractUserId(principal);
        if (userId == null) return;
        matchmakingService.leaveQueue(userId);
    }

    /**
     * The Principal can be:
     * 1. Our StompPrincipal (from WebSocketConfig CONNECT auth) — name is userId string
     * 2. UsernamePasswordAuthenticationToken (from JwtAuthFilter) — principal is User entity
     */
    private Long extractUserId(Principal principal) {
        if (principal == null) return null;

        if (principal instanceof UsernamePasswordAuthenticationToken auth
                && auth.getPrincipal() instanceof User user) {
            return user.getId();
        }

        // Fallback: try parsing principal.getName() as a long (StompPrincipal case)
        try {
            return Long.parseLong(principal.getName());
        } catch (NumberFormatException e) {
            log.warn("Could not extract userId from principal: {}", principal.getClass().getName());
            return null;
        }
    }
}
