package com.inspectron.inspectron.infra.security;

import com.inspectron.inspectron.api.domain.user.entity.User;
import com.inspectron.inspectron.api.repository.UserRepository;
import com.inspectron.inspectron.api.service.AuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthService authService;
    private final UserRepository userRepository;

    // Inspeciona cada requisição, valida o token e autentica o usuário na SecurityContext.
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith(BEARER_PREFIX)) {
            String token = authorizationHeader.substring(BEARER_PREFIX.length());
            Optional<UUID> userId = authService.extractUserIdFromAccessToken(token);

            if (userId.isPresent() && SecurityContextHolder.getContext().getAuthentication() == null) {
                userRepository
                        .findById(userId.get())
                        .filter(user -> !user.isDisabled())
                        .ifPresent(user -> setAuthentication(user, request));
            }
        }

        filterChain.doFilter(request, response);
    }

    // Define o usuário autenticado no contexto de segurança a partir dos dados do token.
    private void setAuthentication(User user, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }
}
