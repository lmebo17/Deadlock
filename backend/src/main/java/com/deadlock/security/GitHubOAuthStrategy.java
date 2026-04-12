package com.deadlock.security;

import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class GitHubOAuthStrategy implements OAuthProviderStrategy {

    @Override
    public String getProviderName() { return "github"; }

    @Override
    public String extractEmail(OAuth2User oauthUser) {
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

    @Override
    public String extractDisplayName(OAuth2User oauthUser) {
        String name = oauthUser.getAttribute("name");
        return name != null ? name : oauthUser.getAttribute("login");
    }

    @Override
    public String extractAvatarUrl(OAuth2User oauthUser) {
        return oauthUser.getAttribute("avatar_url");
    }

    @Override
    public String extractProviderId(OAuth2User oauthUser) {
        return oauthUser.getAttribute("id").toString();
    }
}
