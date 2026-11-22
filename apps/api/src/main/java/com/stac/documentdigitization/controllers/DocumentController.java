package com.stac.documentdigitization.controllers;

import com.stac.documentdigitization.dtos.requests.DocumentCreateRequestDto;
import com.stac.documentdigitization.dtos.responses.DocumentResponseDto;
import com.stac.documentdigitization.services.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Owner-scoped document metadata and library filters. */
@RestController
@RequestMapping("/api/v1/documents")
@Tag(name = "Documents")
@RequiredArgsConstructor
public class DocumentController {
    private final DocumentService documents;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create owned document metadata")
    public DocumentResponseDto create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody DocumentCreateRequestDto request) {
        return documents.create(UUID.fromString(jwt.getSubject()), request);
    }

    @GetMapping
    @Operation(summary = "List owned documents with optional subject and date filters")
    public List<DocumentResponseDto> list(@AuthenticationPrincipal Jwt jwt,
                                       @RequestParam(required = false) UUID subjectId,
                                       @RequestParam(required = false) LocalDate date) {
        return documents.list(UUID.fromString(jwt.getSubject()), subjectId, date);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get owned document metadata")
    public DocumentResponseDto get(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return documents.get(UUID.fromString(jwt.getSubject()), id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete owned document metadata")
    public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        documents.delete(UUID.fromString(jwt.getSubject()), id);
    }
}
