package com.inspectron.inspectron.api.service;

import com.inspectron.inspectron.api.config.JwtProperties;
import com.inspectron.inspectron.api.domain.auth.dto.AuthResponse;
import com.inspectron.inspectron.api.domain.auth.dto.CredentialsRequest;
import com.inspectron.inspectron.api.domain.user.dto.UserResponse;
import com.inspectron.inspectron.api.domain.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final JwtProperties jwtProperties;

    // Realiza o fluxo de login e emite os tokens apropriados para o usuário.
    public AuthResponse login(CredentialsRequest credentials) {
        User user = userService
                .checkCredentials(credentials)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid-credentials"));

        UserResponse userResponse = UserResponse.from(user);

        if (user.isDefaultPassword()) {
            String firstAccessToken = generateFirstAccessToken(user);
            long expiresIn = getFirstAccessTokenExpirationSeconds();
            return AuthResponse.firstAccess(userResponse, firstAccessToken, expiresIn);
        }

        String token = generateAccessToken(user, credentials.isRememberMe());
        String assetsToken = generateAssetsToken(user, credentials.isRememberMe());
        long expiresIn = getAccessTokenExpirationSeconds(credentials.isRememberMe());

        return AuthResponse.regular(userResponse, token, assetsToken, expiresIn);
    }

    // Recupera o identificador do usuário a partir do token de primeiro acesso.
    public UUID findUserIdFromFirstAccessToken(String token) {
        return extractUserIdFromFirstAccessToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid-token"));
    }

    // Gera o token de acesso usado nas requisições protegidas.
    public String generateAccessToken(User user, boolean rememberMe) {
        Duration ttl = rememberMe
                ? jwtProperties.getRememberMeAccessTokenExpiration()
                : jwtProperties.getAccessTokenExpiration();
        return buildToken(user, ttl, jwtProperties.getSecret(), user.isFirstAccess());
    }

    // Gera o token para acesso a recursos estáticos/externos.
    public String generateAssetsToken(User user, boolean rememberMe) {
        Duration ttl = rememberMe
                ? jwtProperties.getRememberMeAccessTokenExpiration()
                : jwtProperties.getAccessTokenExpiration();
        Instant now = Instant.now();
        Instant expiration = now.plus(ttl);

        return Jwts.builder()
                .subject(user.getId().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .claim("role", user.getRole().name())
                .signWith(signingKey(jwtProperties.getAssetsSecret()), Jwts.SIG.HS256)
                .compact();
    }

    // Gera o token temporário utilizado no primeiro acesso para troca de senha.
    public String generateFirstAccessToken(User user) {
        return buildToken(
                user,
                jwtProperties.getFirstAccessTokenExpiration(),
                jwtProperties.getFirstAccessSecret(),
                true);
    }

    // Extrai o ID do usuário a partir do token de acesso regular.
    public Optional<UUID> extractUserIdFromAccessToken(String token) {
        return parseClaims(token, jwtProperties.getSecret()).map(claims -> UUID.fromString(claims.getSubject()));
    }

    // Extrai o ID do usuário a partir do token de primeiro acesso.
    public Optional<UUID> extractUserIdFromFirstAccessToken(String token) {
        return parseClaims(token, jwtProperties.getFirstAccessSecret())
                .map(claims -> UUID.fromString(claims.getSubject()));
    }

    // Retorna o tempo de expiração do token de acesso em segundos, respeitando o rememberMe.
    public long getAccessTokenExpirationSeconds(boolean rememberMe) {
        Duration ttl = rememberMe
                ? jwtProperties.getRememberMeAccessTokenExpiration()
                : jwtProperties.getAccessTokenExpiration();
        return ttl.toSeconds();
    }

    // Retorna o tempo de expiração do token de primeiro acesso em segundos.
    public long getFirstAccessTokenExpirationSeconds() {
        return jwtProperties.getFirstAccessTokenExpiration().toSeconds();
    }

    // Monta o token JWT com as claims principais do usuário.
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

    // Converte o token em claims, retornando vazio quando inválido.
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

    // Cria a chave HMAC utilizada na assinatura dos tokens.
    private SecretKey signingKey(String secret) {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
