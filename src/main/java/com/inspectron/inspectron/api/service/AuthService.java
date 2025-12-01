package com.inspectron.inspectron.api.service;

import com.inspectron.inspectron.api.domain.auth.dto.AuthResponse;
import com.inspectron.inspectron.api.domain.auth.dto.CredentialsRequest;
import com.inspectron.inspectron.api.domain.user.dto.UserResponse;
import com.inspectron.inspectron.api.domain.user.entity.User;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final JwtTokenService jwtTokenService;

    public AuthResponse login(CredentialsRequest credentials) {
        User user = userService
                .checkCredentials(credentials)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid-credentials"));

        UserResponse userResponse = UserResponse.from(user);

        if (user.isDefaultPassword()) {
            String firstAccessToken = jwtTokenService.generateFirstAccessToken(user);
            long expiresIn = jwtTokenService.getFirstAccessTokenExpirationSeconds();
            return AuthResponse.firstAccess(userResponse, firstAccessToken, expiresIn);
        }

        String token = jwtTokenService.generateAccessToken(user, credentials.isRememberMe());
        String assetsToken = jwtTokenService.generateAssetsToken(user, credentials.isRememberMe());
        long expiresIn = jwtTokenService.getAccessTokenExpirationSeconds(credentials.isRememberMe());

        return AuthResponse.regular(userResponse, token, assetsToken, expiresIn);
    }

    public UUID findUserIdFromFirstAccessToken(String token) {
        return jwtTokenService
                .extractUserIdFromFirstAccessToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid-token"));
    }
}
