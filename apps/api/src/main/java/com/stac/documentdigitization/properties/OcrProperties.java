package com.stac.documentdigitization.properties;

import java.net.URI;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Private OCR service location and shared internal token. */
@ConfigurationProperties(prefix = "app.ocr")
public record OcrProperties(URI url, String token) {}
