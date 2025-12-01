package com.inspectron.inspectron.api.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CredentialsRequest {

    @NotBlank(message = "empty-enrollment")
    @Size(max = 10, message = "maxlength-enrollment")
    @Pattern(regexp = "^[0-9]*$", message = "non-numeric-enrollment")
    @Pattern(regexp = "^.{6,}$", message = "minlength-enrollment")
    private String enrollment;

    @NotBlank(message = "empty-password")
    @Size(max = 64, message = "maxlength-password")
    @Pattern(regexp = "^.{6,}$", message = "minlength-password")
    private String password;

    private boolean rememberMe;
}
