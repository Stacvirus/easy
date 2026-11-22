package com.stac.documentdigitization.exceptions;

/** Password exceeds BCrypt's 72-byte input limit. */
public class PasswordTooLongException extends BusinessException {
    public PasswordTooLongException() {
        super(ErrorCode.PASSWORD_TOO_LONG);
    }
}
