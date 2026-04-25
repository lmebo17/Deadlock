package com.deadlock.config;

import com.deadlock.model.User;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.security.Principal;

@Slf4j
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

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
                    rebindPrincipalToUserId(accessor);
                }
                return message;
            }
        });
    }

    /**
     * The HTTP handshake already authenticated the user via JwtAuthFilter (cookie-based)
     * and populated the WebSocket session's Principal as a UsernamePasswordAuthenticationToken
     * with the User entity as principal. The default Principal.getName() returns the User's
     * toString() (object hash), which breaks user-destination routing. Replace it with a
     * stable name = userId string.
     */
    private void rebindPrincipalToUserId(StompHeaderAccessor accessor) {
        Principal existing = accessor.getUser();
        if (existing instanceof UsernamePasswordAuthenticationToken auth
                && auth.getPrincipal() instanceof User user) {
            accessor.setUser(new StompPrincipal(user.getId().toString()));
            log.debug("WebSocket CONNECT: rebound principal to userId={}", user.getId());
        } else {
            log.warn("WebSocket CONNECT: no authenticated user (got {})",
                    existing == null ? "null" : existing.getClass().getSimpleName());
        }
    }

    private record StompPrincipal(String name) implements Principal {
        @Override
        public String getName() { return name; }
    }
}
