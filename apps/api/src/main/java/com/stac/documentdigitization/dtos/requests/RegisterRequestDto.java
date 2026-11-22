package com.stac.documentdigitization.dtos.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** New account credentials; the password is never included in logs. */
public record RegisterRequestDto(
        @Schema(description = "Account email address", example = "student@example.com")
        @NotBlank(message = "{validation.required}")
        @Email(message = "{validation.email}")
        @Size(max = 320, message = "{validation.size}") String email,
        @Schema(description = "Password with at least 8 characters", format = "password",
                accessMode = Schema.AccessMode.WRITE_ONLY)
        @NotBlank(message = "{validation.required}")
        @Size(min = 8, max = 72, message = "{validation.size}") String password) {
    public RegisterRequestDto {
        email = email == null ? null : email.trim();
    }

    @Override
    public String toString() {
        return "RegisterRequestDto[email=" + email + ", password=***]";
    }
}
