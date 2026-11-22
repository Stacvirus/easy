package com.stac.documentdigitization.controllers;

import com.stac.documentdigitization.dtos.requests.PageOrderRequestDto;
import com.stac.documentdigitization.dtos.responses.PageResponseDto;
import com.stac.documentdigitization.services.PageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** Owned image uploads, previews and persisted page ordering. */
@RestController
@RequestMapping("/api/v1/documents/{documentId}/pages")
@Tag(name = "Pages")
@RequiredArgsConstructor
public class PageController {
    private final PageService pages;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Upload one or more JPEG or PNG pages")
    public List<PageResponseDto> upload(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID documentId,
                                        @RequestPart("files") List<MultipartFile> files) {
        return pages.upload(UUID.fromString(jwt.getSubject()), documentId, files);
    }

    @GetMapping
    @Operation(summary = "List pages in their saved order")
    public List<PageResponseDto> list(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID documentId) {
        return pages.list(UUID.fromString(jwt.getSubject()), documentId);
    }

    @PutMapping("/order")
    @Operation(summary = "Replace the complete page order")
    public List<PageResponseDto> reorder(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID documentId,
                                         @Valid @RequestBody PageOrderRequestDto request) {
        return pages.reorder(UUID.fromString(jwt.getSubject()), documentId, request.pageIds());
    }

    @GetMapping("/{pageId}/content")
    @Operation(summary = "Read an original image for preview")
    public ResponseEntity<byte[]> read(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID documentId,
                                       @PathVariable UUID pageId) {
        PageService.PageContent content = pages.read(UUID.fromString(jwt.getSubject()), documentId, pageId);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .contentType(MediaType.parseMediaType(content.contentType())).body(content.bytes());
    }

    @DeleteMapping("/{pageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove an owned page and close the position gap")
    public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID documentId, @PathVariable UUID pageId) {
        pages.delete(UUID.fromString(jwt.getSubject()), documentId, pageId);
    }
}
