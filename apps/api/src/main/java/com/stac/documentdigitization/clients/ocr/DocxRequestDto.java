package com.stac.documentdigitization.clients.ocr;

import java.util.List;

/** Ordered recognized pages sent to the Python DOCX renderer. */
public record DocxRequestDto(List<RecognizedPageDto> pages) {}
