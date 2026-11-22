package com.stac.documentdigitization.exceptions;

import lombok.Getter;

/** Base for domain failures translated by the API's localized exception handler. */
@Getter
public abstract class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;
    private final transient Object[] args;

    protected BusinessException(ErrorCode errorCode, Object... args) {
        super(errorCode.name());
        this.errorCode = errorCode;
        this.args = args;
    }
}
