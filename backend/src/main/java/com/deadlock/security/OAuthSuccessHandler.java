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

@Component
public class OAuthSuccessHandler implements AuthenticationSuccessHandler {

    private final UserService userService;
    private final JwtService jwtService;
    private final String frontendUrl;

    public OAuthSuccessHandler(UserService userService, JwtService jwtService,
                                @Value("${app.frontend-url}") String frontendUrl) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.frontendUrl = frontendUrl;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                         Authentication authentication) throws IOException {
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oauthUser = oauthToken.getPrincipal();
        String provider = oauthToken.getAuthorizedClientRegistrationId();

        String email = extractEmail(oauthUser, provider);
        String displayName = extractDisplayName(oauthUser, provider);
        String avatarUrl = extractAvatarUrl(oauthUser, provider);
        String providerId = extractProviderId(oauthUser, provider);

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

    @SuppressWarnings("unchecked")
    private String extractEmail(OAuth2User oauthUser, String provider) {
        if ("github".equals(provider)) {
            String email = oauthUser.getAttribute("email");
            if (email != null) return email;
            Object emailsAttr = oauthUser.getAttribute("emails");
            if (emailsAttr instanceof List<?> emails && !emails.isEmpty()) {
                for (Object e : emails) {
                    if (e instanceof Map<?, ?> emailMap) {
                        Boolean primary = (Boolean) emailMap.get("primary");
                        if (Boolean.TRUE.equals(primary)) {
                            return (String) emailMap.get("email");
                        }
                    }
                }
            }
            return oauthUser.getAttribute("login") + "@github.users.noreply.com";
        }
        return oauthUser.getAttribute("email");
    }

    private String extractDisplayName(OAuth2User oauthUser, String provider) {
        if ("github".equals(provider)) {
            String name = oauthUser.getAttribute("name");
            return name != null ? name : oauthUser.getAttribute("login");
        }
        return oauthUser.getAttribute("name");
    }

    private String extractAvatarUrl(OAuth2User oauthUser, String provider) {
        if ("github".equals(provider)) {
            return oauthUser.getAttribute("avatar_url");
        }
        return oauthUser.getAttribute("picture");
    }

    private String extractProviderId(OAuth2User oauthUser, String provider) {
        if ("github".equals(provider)) {
            return oauthUser.getAttribute("id").toString();
        }
        return oauthUser.getAttribute("sub");
    }
}
