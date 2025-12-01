package com.inspectron.inspectron.api.domain.user.dto;

import com.inspectron.inspectron.api.domain.user.entity.User;
import com.inspectron.inspectron.api.domain.user.enums.UserRole;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UserResponse {

    private final UUID id;
    private final String name;
    private final String enrollment;
    private final UserRole role;
    private final String email;
    private final boolean disabled;
    private final boolean firstAccess;
    private final boolean tutorial;
    private final OffsetDateTime createdAt;
    private final OffsetDateTime updatedAt;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .enrollment(user.getEnrollment())
                .role(user.getRole())
                .email(user.getEmail())
                .disabled(user.isDisabled())
                .firstAccess(user.isFirstAccess())
                .tutorial(user.isTutorial())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
