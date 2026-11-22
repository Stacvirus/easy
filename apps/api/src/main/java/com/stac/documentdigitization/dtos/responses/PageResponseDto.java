package com.stac.documentdigitization.dtos.responses;

import java.time.Instant;
import java.util.UUID;

/** Ordered page metadata; storage keys are never exposed to clients. */
public record PageResponseDto(UUID id, int position, String contentType, long sizeBytes, Instant uploadedAt) {}
