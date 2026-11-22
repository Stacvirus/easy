package com.stac.documentdigitization.controllers;

import com.stac.documentdigitization.dtos.responses.DocumentExportResponseDto;
import com.stac.documentdigitization.services.DocumentExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Export metadata and authorized DOCX download routes. */
@RestController
@RequestMapping("/api/v1/documents/{documentId}/exports")
@Tag(name = "Exports")
@RequiredArgsConstructor
public class ExportController {
    private static final MediaType DOCX_TYPE = MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
    private final DocumentExportService exports;

    @GetMapping
    @Operation(summary = "List completed Word exports")
    public List<DocumentExportResponseDto> list(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID documentId) {
        return exports.list(UUID.fromString(jwt.getSubject()), documentId);
    }

    @GetMapping("/{exportId}/download")
    @Operation(summary = "Download an owned Word export")
    public ResponseEntity<byte[]> download(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID documentId,
                                           @PathVariable UUID exportId) {
        DocumentExportService.ExportContent content = exports.download(
                UUID.fromString(jwt.getSubject()), documentId, exportId);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).contentType(DOCX_TYPE)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"document-" + content.exportId() + ".docx\"")
                .body(content.bytes());
    }
}
