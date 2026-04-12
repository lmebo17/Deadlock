package com.deadlock.security;

import com.deadlock.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    public static final String COOKIE_NAME = "deadlock_token";

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        String token = extractTokenFromCookie(request);

        if (token != null && jwtService.validateToken(token)) {
            try {
                Long userId = jwtService.extractUserId(token);
                int tokenVersion = jwtService.extractTokenVersion(token);

                userRepository.findById(userId).ifPresent(user -> {
                    if (user.getTokenVersion() == tokenVersion) {
                        var authorities = List.of(
                                new SimpleGrantedAuthority("ROLE_" + user.getRole()));
                        var auth = new UsernamePasswordAuthenticationToken(
                                user, null, authorities);
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                });
            } catch (Exception e) {
                log.warn("Failed to process JWT token", e);
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(c -> COOKIE_NAME.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}
