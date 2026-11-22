package com.stac.documentdigitization.mappers;

import com.stac.documentdigitization.dtos.responses.AuthResponseDto;
import com.stac.documentdigitization.entities.AppUser;
import java.time.Instant;
import org.springframework.stereotype.Component;

/** Combines verified account identity with an issued token. */
@Component
public class AuthMapper {
    public AuthResponseDto toResponse(AppUser user, String token, Instant expiresAt) {
        return new AuthResponseDto(token, expiresAt, user.getId(), user.getEmail());
    }
}
