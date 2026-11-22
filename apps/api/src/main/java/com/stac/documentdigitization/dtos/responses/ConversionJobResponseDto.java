package com.stac.documentdigitization.dtos.responses;

import com.stac.documentdigitization.entities.ConversionStatus;
import java.time.Instant;
import java.util.UUID;

/** Persisted conversion state without internal object keys or exception traces. */
public record ConversionJobResponseDto(UUID id, UUID documentId, ConversionStatus status, String language,
                                       Instant createdAt, Instant startedAt, Instant finishedAt,
                                       String failureCode, String failureMessage) {}
