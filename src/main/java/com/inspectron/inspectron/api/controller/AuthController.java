package com.inspectron.inspectron.api.controller;

import com.inspectron.inspectron.api.domain.auth.dto.AuthResponse;
import com.inspectron.inspectron.api.domain.auth.dto.CredentialsRequest;
import com.inspectron.inspectron.api.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody CredentialsRequest credentialsRequest) {
        return ResponseEntity.ok(authService.login(credentialsRequest));
    }
}
