package com.deadlock.security;

import com.deadlock.model.User;
import com.deadlock.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class OAuthSuccessHandler implements AuthenticationSuccessHandler {

    private final UserService userService;
    private final JwtService jwtService;
    private final String frontendUrl;
    private final Map<String, OAuthProviderStrategy> strategies;

    public OAuthSuccessHandler(UserService userService, JwtService jwtService,
                                @Value("${app.frontend-url}") String frontendUrl,
                                List<OAuthProviderStrategy> strategyList) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.frontendUrl = frontendUrl;
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(OAuthProviderStrategy::getProviderName, Function.identity()));
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                         Authentication authentication) throws IOException {
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oauthUser = oauthToken.getPrincipal();
        String provider = oauthToken.getAuthorizedClientRegistrationId();

        OAuthProviderStrategy strategy = strategies.get(provider);
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported OAuth provider: " + provider);
        }

        String email = strategy.extractEmail(oauthUser);
        String displayName = strategy.extractDisplayName(oauthUser);
        String avatarUrl = strategy.extractAvatarUrl(oauthUser);
        String providerId = strategy.extractProviderId(oauthUser);

        User user = userService.findOrCreateUser(email, displayName, avatarUrl, provider, providerId);

        String jwt = jwtService.generateToken(user);

        Cookie cookie = new Cookie(JwtAuthFilter.COOKIE_NAME, jwt);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(86400);
        cookie.setSecure(false);
        response.addCookie(cookie);

        String redirectUrl = user.getUsername() == null
                ? frontendUrl + "/setup-username"
                : frontendUrl + "/lobby";

        response.sendRedirect(redirectUrl);
    }
}
