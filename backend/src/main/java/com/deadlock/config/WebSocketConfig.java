package com.deadlock.config;

import com.deadlock.security.JwtAuthFilter;
import com.deadlock.security.JwtService;
import com.deadlock.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.security.Principal;
import java.util.List;

@Slf4j
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins(frontendUrl);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor =
                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    authenticate(accessor);
                }
                return message;
            }
        });
    }

    private void authenticate(StompHeaderAccessor accessor) {
        // The JWT cookie is sent by the browser during the WebSocket handshake.
        // We extract it from the native HTTP Cookie header.
        List<String> cookieHeaders = accessor.getNativeHeader("cookie");
        String token = extractTokenFromCookies(cookieHeaders);

        // Fallback: also accept Authorization header for non-browser clients
        if (token == null) {
            List<String> authHeaders = accessor.getNativeHeader("Authorization");
            if (authHeaders != null && !authHeaders.isEmpty() && authHeaders.get(0).startsWith("Bearer ")) {
                token = authHeaders.get(0).substring(7);
            }
        }

        if (token == null || !jwtService.validateToken(token)) {
            log.warn("WebSocket CONNECT rejected: missing or invalid JWT");
            return;
        }

        try {
            Long userId = jwtService.extractUserId(token);
            int tokenVersion = jwtService.extractTokenVersion(token);
            var userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty() || userOpt.get().getTokenVersion() != tokenVersion) {
                log.warn("WebSocket CONNECT rejected: user not found or token version mismatch");
                return;
            }
            // Set the principal — userId as the name so SimpMessagingTemplate.convertAndSendToUser works
            accessor.setUser(new StompPrincipal(userId.toString()));
            log.debug("WebSocket CONNECT authenticated user {}", userId);
        } catch (RuntimeException e) {
            log.warn("WebSocket CONNECT failed during JWT processing", e);
        }
    }

    private String extractTokenFromCookies(List<String> cookieHeaders) {
        if (cookieHeaders == null || cookieHeaders.isEmpty()) return null;
        // cookieHeaders.get(0) looks like: "deadlock_token=xxx; other=yyy"
        for (String header : cookieHeaders) {
            for (String pair : header.split(";")) {
                String trimmed = pair.trim();
                if (trimmed.startsWith(JwtAuthFilter.COOKIE_NAME + "=")) {
                    return trimmed.substring(JwtAuthFilter.COOKIE_NAME.length() + 1);
                }
            }
        }
        return null;
    }

    private record StompPrincipal(String name) implements Principal {
        @Override
        public String getName() { return name; }
    }
}
