package com.stac.documentdigitization.services;

import com.stac.documentdigitization.configurations.SecurityConfiguration;
import com.stac.documentdigitization.dtos.requests.LoginRequestDto;
import com.stac.documentdigitization.dtos.requests.RegisterRequestDto;
import com.stac.documentdigitization.dtos.responses.AuthResponseDto;
import com.stac.documentdigitization.entities.AppUser;
import com.stac.documentdigitization.exceptions.EmailAlreadyRegisteredException;
import com.stac.documentdigitization.exceptions.InvalidCredentialsException;
import com.stac.documentdigitization.exceptions.PasswordTooLongException;
import com.stac.documentdigitization.mappers.AuthMapper;
import com.stac.documentdigitization.repositories.AppUserRepository;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Registers users and issues short-lived tokens after password verification. */
@Service
@RequiredArgsConstructor
public class AuthService {
    private final AppUserRepository users;
    private final PasswordEncoder passwords;
    private final JwtEncoder tokens;
    private final AuthMapper mapper;

    /** Normalize the email, hash the password and create the account atomically. */
    @Transactional
    public AuthResponseDto register(RegisterRequestDto request) {
        String email = normalizeEmail(request.email());
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new PasswordTooLongException();
        }
        if (users.existsByEmail(email)) {
            throw new EmailAlreadyRegisteredException();
        }
        AppUser user = users.saveAndFlush(new AppUser(email, passwords.encode(request.password())));
        return issueToken(user);
    }

    /** Verify credentials without revealing which part was incorrect. */
    @Transactional(readOnly = true)
    public AuthResponseDto login(LoginRequestDto request) {
        AppUser user = users.findByEmail(normalizeEmail(request.email()))
                .orElseThrow(AuthService::invalidCredentials);
        if (!passwords.matches(request.password(), user.getPasswordHash())) {
            throw invalidCredentials();
        }
        return issueToken(user);
    }

    private AuthResponseDto issueToken(AppUser user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(12, ChronoUnit.HOURS);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(SecurityConfiguration.ISSUER)
                .subject(user.getId().toString())
                .issuedAt(now)
                .expiresAt(expiresAt)
                .build();
        String token = tokens.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
        return mapper.toResponse(user, token, expiresAt);
    }

    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private static InvalidCredentialsException invalidCredentials() {
        return new InvalidCredentialsException();
    }
}
