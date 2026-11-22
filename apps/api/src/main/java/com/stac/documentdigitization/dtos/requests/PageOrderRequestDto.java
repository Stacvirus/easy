package com.stac.documentdigitization.dtos.requests;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

/** Complete desired order of a document's current page IDs. */
public record PageOrderRequestDto(@NotNull(message = "{validation.required}")
                                  List<@NotNull(message = "{validation.required}") UUID> pageIds) {}
