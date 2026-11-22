package com.stac.documentdigitization.dtos.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;

/** OCR language for a new immutable page-order snapshot; English is the default. */
public record ConversionCreateRequestDto(
        @Schema(description = "Recognition language", allowableValues = {"en", "fr"}, example = "en")
        @Pattern(regexp = "en|fr", message = "{validation.ocr.language}") String language) {
    public ConversionCreateRequestDto {
        language = language == null ? "en" : language;
    }
}
