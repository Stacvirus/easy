package com.stac.documentdigitization.exceptions;

/** Export is absent from an owned document. */
public class ExportNotFoundException extends BusinessException {
    public ExportNotFoundException() { super(ErrorCode.EXPORT_NOT_FOUND); }
}
