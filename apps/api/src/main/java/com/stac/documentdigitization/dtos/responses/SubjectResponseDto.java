package com.stac.documentdigitization.dtos.responses;

import java.time.Instant;
import java.util.UUID;

/** Public metadata for a subject owned by the requesting user. */
public record SubjectResponseDto(UUID id, String name, Instant createdAt) {}
