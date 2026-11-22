package com.stac.documentdigitization.dtos.responses;

import java.time.Instant;
import java.util.UUID;

/** One downloadable, immutable Word export. */
public record DocumentExportResponseDto(UUID id, UUID jobId, long sizeBytes, Instant createdAt) {}
