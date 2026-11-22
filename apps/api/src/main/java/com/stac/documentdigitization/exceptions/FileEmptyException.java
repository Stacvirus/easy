package com.stac.documentdigitization.exceptions;

/** Uploaded file has no bytes. */
public class FileEmptyException extends BusinessException {
    public FileEmptyException() { super(ErrorCode.FILE_EMPTY); }
}
