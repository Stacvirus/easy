package com.stac.documentdigitization.clients.ocr;

import java.util.List;

/** One line recognized by the Python service, with optional model score and box. */
public record RecognizedLineDto(String text, Double confidence, List<Integer> box) {}
