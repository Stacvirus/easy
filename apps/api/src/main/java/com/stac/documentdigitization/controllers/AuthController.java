package com.stac.documentdigitization.controllers;

import com.stac.documentdigitization.dtos.requests.LoginRequestDto;
import com.stac.documentdigitization.dtos.requests.RegisterRequestDto;
import com.stac.documentdigitization.dtos.responses.AuthResponseDto;
import com.stac.documentdigitization.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Public registration and login endpoints; issued tokens identify the account by UUID. */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService auth;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirements
    @Operation(summary = "Register an account and issue a bearer token")
    public AuthResponseDto register(@Valid @RequestBody RegisterRequestDto request) {
        return auth.register(request);
    }

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(summary = "Log in and issue a bearer token")
    public AuthResponseDto login(@Valid @RequestBody LoginRequestDto request) {
        return auth.login(request);
    }
}
