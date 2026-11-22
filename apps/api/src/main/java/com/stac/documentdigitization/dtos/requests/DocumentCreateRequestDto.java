package com.stac.documentdigitization.dtos.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

/** Document metadata; the web client supplies its selected or current local date. */
public record DocumentCreateRequestDto(
        @Schema(description = "Document title", example = "Lecture 1 notes")
        @NotBlank(message = "{validation.required}")
        @Size(max = 200, message = "{validation.size}") String title,
        @Schema(description = "Date selected for organizing the document", example = "2026-11-22")
        @NotNull(message = "{validation.required}") LocalDate documentDate,
        @Schema(description = "Owned subject ID; omit for a general document")
        UUID subjectId) {
    public DocumentCreateRequestDto {
        title = title == null ? null : title.trim();
    }
}
