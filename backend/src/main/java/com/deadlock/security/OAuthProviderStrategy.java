package com.deadlock.security;

import org.springframework.security.oauth2.core.user.OAuth2User;

public interface OAuthProviderStrategy {
    String getProviderName();
    String extractEmail(OAuth2User oauthUser);
    String extractDisplayName(OAuth2User oauthUser);
    String extractAvatarUrl(OAuth2User oauthUser);
    String extractProviderId(OAuth2User oauthUser);
}
