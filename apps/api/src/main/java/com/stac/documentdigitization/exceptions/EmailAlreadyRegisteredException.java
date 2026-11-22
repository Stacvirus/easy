package com.stac.documentdigitization.exceptions;

/** Account registration would reuse an existing email. */
public class EmailAlreadyRegisteredException extends BusinessException {
    public EmailAlreadyRegisteredException() {
        super(ErrorCode.EMAIL_EXISTS);
    }
}
