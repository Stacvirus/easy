package com.stac.documentdigitization.controllers;

import com.stac.documentdigitization.exceptions.ApiErrorDto;
import com.stac.documentdigitization.exceptions.BusinessException;
import com.stac.documentdigitization.exceptions.ErrorCode;
import com.stac.documentdigitization.services.I18nService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/** Converts expected application and validation failures into consistent JSON errors. */
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private final I18nService i18n;

    @ExceptionHandler(BusinessException.class)
    ResponseEntity<ApiErrorDto> handleBusiness(BusinessException error, HttpServletRequest request) {
        return respond(error.getErrorCode(), request, Map.of(), error.getArgs());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiErrorDto> handleValidation(MethodArgumentNotValidException error, HttpServletRequest request) {
        Map<String, String> fields = error.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(field -> field.getField(),
                        field -> field.isBindingFailure() || field.getDefaultMessage() == null
                                ? i18n.message("validation.invalid.value") : field.getDefaultMessage(),
                        (first, ignored) -> first));
        return respond(ErrorCode.VALIDATION_ERROR, request, fields);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiErrorDto> handleMalformed(HttpMessageNotReadableException error, HttpServletRequest request) {
        return respond(ErrorCode.MALFORMED_REQUEST, request, Map.of());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ApiErrorDto> handleInvalidParameter(MethodArgumentTypeMismatchException error,
                                                    HttpServletRequest request) {
        return respond(ErrorCode.INVALID_PARAMETER, request, Map.of(), error.getName());
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    ResponseEntity<ApiErrorDto> handleMethodValidation(HandlerMethodValidationException error,
                                                    HttpServletRequest request) {
        return respond(ErrorCode.VALIDATION_ERROR, request, Map.of());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    ResponseEntity<ApiErrorDto> handleMissingParameter(MissingServletRequestParameterException error,
                                                    HttpServletRequest request) {
        return respond(ErrorCode.MISSING_PARAMETER, request, Map.of(), error.getParameterName());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ResponseEntity<ApiErrorDto> handleMethodNotAllowed(HttpRequestMethodNotSupportedException error,
                                                    HttpServletRequest request) {
        return respond(ErrorCode.METHOD_NOT_ALLOWED, request, Map.of(), error.getMethod());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    ResponseEntity<ApiErrorDto> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException error,
                                                        HttpServletRequest request) {
        return respond(ErrorCode.UNSUPPORTED_MEDIA_TYPE, request, Map.of(), error.getContentType());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiErrorDto> handleConflict(DataIntegrityViolationException error, HttpServletRequest request) {
        return respond(ErrorCode.CONFLICT, request, Map.of());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    ResponseEntity<ApiErrorDto> handleMissingResource(NoResourceFoundException error, HttpServletRequest request) {
        return respond(ErrorCode.ENDPOINT_NOT_FOUND, request, Map.of());
    }

    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<ApiErrorDto> handleAuthentication(AuthenticationException error, HttpServletRequest request) {
        return respond(ErrorCode.UNAUTHENTICATED, request, Map.of());
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ApiErrorDto> handleAccessDenied(AccessDeniedException error, HttpServletRequest request) {
        return respond(ErrorCode.FORBIDDEN, request, Map.of());
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiErrorDto> handleUnexpected(Exception error, HttpServletRequest request) {
        log.error("Unexpected failure on {}", request.getRequestURI(), error);
        return respond(ErrorCode.INTERNAL_ERROR, request, Map.of());
    }

    private ResponseEntity<ApiErrorDto> respond(ErrorCode code, HttpServletRequest request,
                                                Map<String, String> fields, Object... args) {
        String message = i18n.message(code.getMessageKey(), args);
        return ResponseEntity.status(code.getStatus())
                .body(ApiErrorDto.of(code, message, request.getRequestURI(), fields));
    }
}
