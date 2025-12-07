package com.inspectron.inspectron.domain.user.dto;

import com.inspectron.inspectron.domain.user.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserRequest {

    @NotNull(message = "empty-id")
    private UUID id;

    @Email(message = "invalid-email")
    @Size(max = 200, message = "maxlength-email")
    private String email;

    @NotBlank(message = "empty-enrollment")
    @Size(max = 10, message = "maxlength-enrollment")
    @Pattern(regexp = "^[0-9]*$", message = "non-numeric-enrollment")
    @Pattern(regexp = "^.{6,}$", message = "minlength-enrollment")
    private String enrollment;

    @NotBlank(message = "empty-name")
    @Size(max = 20, message = "maxlength-name")
    private String name;

    @NotNull(message = "empty-role")
    private UserRole role;
}
