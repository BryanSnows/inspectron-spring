package com.inspectron.inspectron.api.controller;

import com.inspectron.inspectron.api.domain.user.dto.CreateUserRequest;
import com.inspectron.inspectron.api.domain.user.dto.PaginationRequest;
import com.inspectron.inspectron.api.domain.user.dto.QueryUserRequest;
import com.inspectron.inspectron.api.domain.user.dto.UpdatePasswordRequest;
import com.inspectron.inspectron.api.domain.user.dto.UpdateUserRequest;
import com.inspectron.inspectron.api.domain.user.dto.UserPageResponse;
import com.inspectron.inspectron.api.domain.user.dto.UserResponse;
import com.inspectron.inspectron.api.domain.user.entity.User;
import com.inspectron.inspectron.api.service.AuthService;
import com.inspectron.inspectron.api.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Validated
@Tag(name = "users")
public class UserController {

    private static final String BEARER_PREFIX = "Bearer ";

    private final UserService userService;
    private final AuthService authService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','ENGINEER')")
    public ResponseEntity<UserPageResponse> findAll(
            @Valid @ParameterObject PaginationRequest pagination,
            @Valid @ParameterObject QueryUserRequest query) {
        return ResponseEntity.ok(userService.findAll(query, pagination));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> findLoggedUser(@AuthenticationPrincipal User loggedUser) {
        if (loggedUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "user-not-found");
        }
        return ResponseEntity.ok(userService.findLoggedUser(loggedUser.getId()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> findOne(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> create(@Valid @RequestBody CreateUserRequest request) {
        userService.create(request);
        return ResponseEntity.ok(new MessageResponse("user-created"));
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> update(@Valid @RequestBody UpdateUserRequest request) {
        userService.update(request);
        return ResponseEntity.ok(new MessageResponse("user-updated"));
    }

    @PutMapping("/update-password")
    public ResponseEntity<MessageResponse> updateLoggedUserPassword(
            @AuthenticationPrincipal User loggedUser, @Valid @RequestBody UpdatePasswordRequest request) {
        if (loggedUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "user-not-found");
        }
        userService.updateLoggedUserPassword(loggedUser.getId(), request);
        return ResponseEntity.ok(new MessageResponse("password-updated"));
    }

    @PutMapping("/update-default-password")
    public ResponseEntity<MessageResponse> updateFirstAccessPassword(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @Valid @RequestBody UpdatePasswordRequest request) {
        UUID userId = authService.findUserIdFromFirstAccessToken(extractToken(authorization));
        userService.updateLoggedUserPassword(userId, request);
        return ResponseEntity.ok(new MessageResponse("password-updated"));
    }

    @PutMapping("/reset-password/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> resetPassword(@PathVariable UUID id) {
        User user = userService
                .findEntityById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user-not-found"));

        userService.updateUserPassword(id, user.getEnrollment());
        return ResponseEntity.ok(new MessageResponse("password-updated"));
    }

    @PutMapping("/disable/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> disable(@PathVariable UUID id) {
        userService.toggleDisable(id);
        return ResponseEntity.ok(new MessageResponse("user-status-updated"));
    }

    @PutMapping("/disable-tutorial/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ENGINEER','OPERATOR')")
    public ResponseEntity<MessageResponse> disableTutorial(@PathVariable UUID id) {
        userService.disableTutorial(id);
        return ResponseEntity.ok(new MessageResponse("user-tutorial-disabled"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> delete(@PathVariable UUID id) {
        User user = userService
                .findEntityById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user-not-found"));

        if ("000001".equals(user.getEnrollment())) {
            return ResponseEntity.ok(new MessageResponse("user-deleted"));
        }

        userService.delete(id);
        return ResponseEntity.ok(new MessageResponse("user-deleted"));
    }

    private String extractToken(String authorizationHeader) {
        if (!StringUtils.hasText(authorizationHeader) || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid-token");
        }
        return authorizationHeader.substring(BEARER_PREFIX.length());
    }
}
