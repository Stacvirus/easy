package com.stac.documentdigitization.exceptions;

/** Login credentials did not identify an account. */
public class InvalidCredentialsException extends BusinessException {
    public InvalidCredentialsException() {
        super(ErrorCode.INVALID_CREDENTIALS);
    }
}
