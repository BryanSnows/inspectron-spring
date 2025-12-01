package com.inspectron.inspectron.api.service;

import com.inspectron.inspectron.api.config.JwtProperties;
import com.inspectron.inspectron.api.domain.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtTokenService {

    private final JwtProperties properties;

    public String generateAccessToken(User user, boolean rememberMe) {
        Duration ttl = rememberMe ? properties.getRememberMeAccessTokenExpiration() : properties.getAccessTokenExpiration();
        return buildToken(user, ttl, properties.getSecret(), user.isFirstAccess());
    }

    public String generateAssetsToken(User user, boolean rememberMe) {
        Duration ttl = rememberMe ? properties.getRememberMeAccessTokenExpiration() : properties.getAccessTokenExpiration();
        Instant now = Instant.now();
        Instant expiration = now.plus(ttl);

        return Jwts.builder()
                .subject(user.getId().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .claim("role", user.getRole().name())
                .signWith(signingKey(properties.getAssetsSecret()), Jwts.SIG.HS256)
                .compact();
    }

    public String generateFirstAccessToken(User user) {
        return buildToken(user, properties.getFirstAccessTokenExpiration(), properties.getFirstAccessSecret(), true);
    }

    public Optional<UUID> extractUserIdFromAccessToken(String token) {
        return parseClaims(token, properties.getSecret()).map(claims -> UUID.fromString(claims.getSubject()));
    }

    public Optional<UUID> extractUserIdFromFirstAccessToken(String token) {
        return parseClaims(token, properties.getFirstAccessSecret()).map(claims -> UUID.fromString(claims.getSubject()));
    }

    public long getAccessTokenExpirationSeconds(boolean rememberMe) {
        Duration ttl = rememberMe ? properties.getRememberMeAccessTokenExpiration() : properties.getAccessTokenExpiration();
        return ttl.toSeconds();
    }

    public long getFirstAccessTokenExpirationSeconds() {
        return properties.getFirstAccessTokenExpiration().toSeconds();
    }

    private String buildToken(User user, Duration ttl, String secret, boolean firstAccess) {
        Instant now = Instant.now();
        Instant expiration = now.plus(ttl);
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", user.getEmail());
        claims.put("name", user.getName());
        claims.put("enrollment", user.getEnrollment());
        claims.put("role", user.getRole().name());
        claims.put("firstAccess", firstAccess);
        claims.put("tutorial", user.isTutorial());

        return Jwts.builder()
                .subject(user.getId().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .claims(claims)
                .signWith(signingKey(secret), Jwts.SIG.HS256)
                .compact();
    }

    private Optional<Claims> parseClaims(String token, String secret) {
        try {
            return Optional.of(
                    Jwts.parser()
                            .verifyWith(signingKey(secret))
                            .build()
                            .parseSignedClaims(token)
                            .getPayload());
        } catch (RuntimeException ex) {
            return Optional.empty();
        }
    }

    private Key signingKey(String secret) {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
