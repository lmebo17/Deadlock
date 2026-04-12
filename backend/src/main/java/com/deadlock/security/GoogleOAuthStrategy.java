package com.deadlock.security;

import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Component
public class GoogleOAuthStrategy implements OAuthProviderStrategy {

    @Override
    public String getProviderName() { return "google"; }

    @Override
    public String extractEmail(OAuth2User oauthUser) {
        return oauthUser.getAttribute("email");
    }

    @Override
    public String extractDisplayName(OAuth2User oauthUser) {
        return oauthUser.getAttribute("name");
    }

    @Override
    public String extractAvatarUrl(OAuth2User oauthUser) {
        return oauthUser.getAttribute("picture");
    }

    @Override
    public String extractProviderId(OAuth2User oauthUser) {
        return oauthUser.getAttribute("sub");
    }
}
