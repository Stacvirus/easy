package com.stac.documentdigitization.dtos.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** Login credentials; the password is never included in logs. */
public record LoginRequestDto(
        @NotBlank(message = "{validation.required}") String email,
        @Schema(format = "password", accessMode = Schema.AccessMode.WRITE_ONLY)
        @NotBlank(message = "{validation.required}") String password) {
    public LoginRequestDto {
        email = email == null ? null : email.trim();
    }

    @Override
    public String toString() {
        return "LoginRequestDto[email=" + email + ", password=***]";
    }
}
