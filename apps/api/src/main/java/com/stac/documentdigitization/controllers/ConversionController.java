package com.stac.documentdigitization.controllers;

import com.stac.documentdigitization.dtos.requests.ConversionCreateRequestDto;
import com.stac.documentdigitization.dtos.responses.ConversionJobResponseDto;
import com.stac.documentdigitization.services.ConversionJobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Submit conversions and inspect their persisted state. */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Conversions")
@RequiredArgsConstructor
public class ConversionController {
    private final ConversionJobService jobs;

    @PostMapping("/documents/{documentId}/conversions")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Queue a conversion from the current page order")
    public ConversionJobResponseDto submit(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID documentId,
                                           @Valid @RequestBody(required = false) ConversionCreateRequestDto request) {
        return jobs.submit(UUID.fromString(jwt.getSubject()), documentId,
                request == null ? "en" : request.language());
    }

    @GetMapping("/conversions/{jobId}")
    @Operation(summary = "Get a conversion's status")
    public ConversionJobResponseDto get(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID jobId) {
        return jobs.get(UUID.fromString(jwt.getSubject()), jobId);
    }

    @GetMapping("/documents/{documentId}/conversions")
    @Operation(summary = "List conversions for an owned document")
    public List<ConversionJobResponseDto> list(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID documentId) {
        return jobs.list(UUID.fromString(jwt.getSubject()), documentId);
    }
}
