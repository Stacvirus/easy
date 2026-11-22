package com.stac.documentdigitization.exceptions;

import com.stac.documentdigitization.configurations.CorrelationIdFilter;
import java.time.Instant;
import java.util.Map;
import lombok.Builder;
import org.slf4j.MDC;

/** Shared error envelope for controller and security failures. */
@Builder
public record ApiErrorDto(Instant timestamp, int status, String errorCode, String message,
                          String path, String correlationId, Map<String, String> fields) {
    public static ApiErrorDto of(ErrorCode code, String message, String path,
                                 Map<String, String> fields) {
        return ApiErrorDto.builder()
                .timestamp(Instant.now())
                .status(code.getStatus().value())
                .errorCode(code.name())
                .message(message)
                .path(path)
                .correlationId(MDC.get(CorrelationIdFilter.MDC_KEY))
                .fields(fields)
                .build();
    }
}
