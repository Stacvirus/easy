package com.stac.documentdigitization.exceptions;

import org.springframework.http.HttpStatus;
import lombok.Getter;

/** Stable client error codes with their HTTP status. */
@Getter
public enum ErrorCode {
    UNAUTHENTICATED(HttpStatus.UNAUTHORIZED, "error.unauthenticated"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "error.forbidden"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "error.invalid.credentials"),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "error.validation"),
    PASSWORD_TOO_LONG(HttpStatus.BAD_REQUEST, "error.password.too.long"),
    MALFORMED_REQUEST(HttpStatus.BAD_REQUEST, "error.malformed.request"),
    MISSING_PARAMETER(HttpStatus.BAD_REQUEST, "error.missing.parameter"),
    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "error.invalid.parameter"),
    ENDPOINT_NOT_FOUND(HttpStatus.NOT_FOUND, "error.endpoint.not.found"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "error.method.not.allowed"),
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "error.unsupported.media.type"),
    EMAIL_EXISTS(HttpStatus.CONFLICT, "error.email.exists"),
    SUBJECT_EXISTS(HttpStatus.CONFLICT, "error.subject.exists"),
    SUBJECT_IN_USE(HttpStatus.CONFLICT, "error.subject.in.use"),
    SUBJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "error.subject.not.found"),
    DOCUMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "error.document.not.found"),
    CONFLICT(HttpStatus.CONFLICT, "error.conflict"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "error.internal");

    private final HttpStatus status;
    private final String messageKey;

    ErrorCode(HttpStatus status, String messageKey) {
        this.status = status;
        this.messageKey = messageKey;
    }
}
