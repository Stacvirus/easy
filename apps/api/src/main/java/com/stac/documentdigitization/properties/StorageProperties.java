package com.stac.documentdigitization.properties;

import java.net.URI;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** MinIO connection and private bucket settings. */
@ConfigurationProperties(prefix = "app.storage")
public record StorageProperties(URI endpoint, String region, String bucket,
                                String accessKey, String secretKey) {}
