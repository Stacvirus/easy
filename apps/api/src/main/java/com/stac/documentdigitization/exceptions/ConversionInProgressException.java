package com.stac.documentdigitization.exceptions;

/** Removing source images would invalidate a pending conversion snapshot. */
public class ConversionInProgressException extends BusinessException {
    public ConversionInProgressException() { super(ErrorCode.CONVERSION_IN_PROGRESS); }
}
