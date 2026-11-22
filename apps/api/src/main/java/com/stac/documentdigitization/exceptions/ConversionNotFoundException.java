package com.stac.documentdigitization.exceptions;

/** Conversion job is absent or belongs to another user. */
public class ConversionNotFoundException extends BusinessException {
    public ConversionNotFoundException() { super(ErrorCode.CONVERSION_NOT_FOUND); }
}
