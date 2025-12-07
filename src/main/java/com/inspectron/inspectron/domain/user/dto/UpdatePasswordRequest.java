package com.inspectron.inspectron.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdatePasswordRequest {

    @NotBlank(message = "empty-currentPassword")
    @Size(max = 64, message = "maxlength-currentPassword")
    @Pattern(regexp = "^.{6,}$", message = "minlength-currentPassword")
    private String currentPassword;

    @NotBlank(message = "empty-newPassword")
    @Size(max = 64, message = "maxlength-newPassword")
    @Pattern(regexp = "^.{6,}$", message = "minlength-newPassword")
    @Pattern(
            regexp = "^(?=.*[!@#$%^&*()_+\\-={}\\[\\]\\|:;\"'<>,.?/])(?=.*[0-9])(?=.*[a-zA-Z]).{6,64}$",
            message = "invalid-newPassword")
    private String newPassword;
}
