package com.stac.documentdigitization.controllers;

import com.stac.documentdigitization.dtos.requests.SubjectRequestDto;
import com.stac.documentdigitization.dtos.responses.SubjectResponseDto;
import com.stac.documentdigitization.services.SubjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Subject routes derive ownership from the verified token, never from request input. */
@RestController
@RequestMapping("/api/v1/subjects")
@Tag(name = "Subjects")
@RequiredArgsConstructor
public class SubjectController {
    private final SubjectService subjects;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a subject")
    public SubjectResponseDto create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody SubjectRequestDto request) {
        return subjects.create(UUID.fromString(jwt.getSubject()), request.name());
    }

    @GetMapping
    @Operation(summary = "List owned subjects")
    public List<SubjectResponseDto> list(@AuthenticationPrincipal Jwt jwt) {
        return subjects.list(UUID.fromString(jwt.getSubject()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an owned subject")
    public SubjectResponseDto get(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return subjects.get(UUID.fromString(jwt.getSubject()), id);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Rename an owned subject")
    public SubjectResponseDto rename(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
                                  @Valid @RequestBody SubjectRequestDto request) {
        return subjects.rename(UUID.fromString(jwt.getSubject()), id, request.name());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete an unused subject")
    public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        subjects.delete(UUID.fromString(jwt.getSubject()), id);
    }
}
