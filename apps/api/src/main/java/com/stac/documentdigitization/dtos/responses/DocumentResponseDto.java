package com.stac.documentdigitization.dtos.responses;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Document metadata without private storage object keys. */
public record DocumentResponseDto(UUID id, String title, LocalDate documentDate, UUID subjectId, Instant createdAt) {}
