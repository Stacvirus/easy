package com.stac.documentdigitization.exceptions;

/** Uploaded bytes do not decode as the declared image type. */
public class InvalidImageException extends BusinessException {
    public InvalidImageException() { super(ErrorCode.INVALID_IMAGE); }
}
