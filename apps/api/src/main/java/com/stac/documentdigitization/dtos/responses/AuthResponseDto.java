package com.stac.documentdigitization.dtos.responses;

import java.time.Instant;
import java.util.UUID;

/** Bearer token and the account it identifies. */
public record AuthResponseDto(String accessToken, Instant expiresAt, UUID userId, String email) {
    /** Avoid leaking a bearer token when a framework logs a response object. */
    @Override
    public String toString() {
        return "AuthResponseDto[accessToken=***, expiresAt=" + expiresAt + ", userId=" + userId + ", email=" + email + "]";
    }
}
