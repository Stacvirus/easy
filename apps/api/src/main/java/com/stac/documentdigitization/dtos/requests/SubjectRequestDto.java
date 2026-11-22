package com.stac.documentdigitization.dtos.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Name for a new or renamed subject. */
public record SubjectRequestDto(
        @Schema(description = "Subject name unique within the account", example = "Mathematics")
        @NotBlank(message = "{validation.required}")
        @Size(max = 120, message = "{validation.size}") String name) {
    public SubjectRequestDto {
        name = name == null ? null : name.trim();
    }
}
