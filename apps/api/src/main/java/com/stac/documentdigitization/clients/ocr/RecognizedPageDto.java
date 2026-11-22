package com.stac.documentdigitization.clients.ocr;

import java.util.List;
import java.util.UUID;

/** Recognized text associated with one snapshotted source page. */
public record RecognizedPageDto(UUID pageId, int position, List<RecognizedLineDto> lines) {}
