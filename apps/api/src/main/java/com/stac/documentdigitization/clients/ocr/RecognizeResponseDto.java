package com.stac.documentdigitization.clients.ocr;

import java.util.List;

/** Structured result for one source image. */
public record RecognizeResponseDto(List<RecognizedLineDto> lines) {}
