package com.inspectron.inspectron.api.domain.auth.dto;

import com.inspectron.inspectron.api.domain.user.dto.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AuthResponse {

    private final UserResponse user;
    private final String token;
    private final String assetsToken;
    private final String firstAccessToken;
    private final long expiresInSeconds;

    public static AuthResponse firstAccess(UserResponse user, String firstAccessToken, long expiresInSeconds) {
        return AuthResponse.builder()
                .user(user)
                .firstAccessToken(firstAccessToken)
                .expiresInSeconds(expiresInSeconds)
                .build();
    }

    public static AuthResponse regular(
            UserResponse user, String token, String assetsToken, long expiresInSeconds) {
        return AuthResponse.builder()
                .user(user)
                .token(token)
                .assetsToken(assetsToken)
                .expiresInSeconds(expiresInSeconds)
                .build();
    }
}
