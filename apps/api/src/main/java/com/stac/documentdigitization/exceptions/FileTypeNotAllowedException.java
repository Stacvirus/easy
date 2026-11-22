package com.stac.documentdigitization.exceptions;

/** Uploaded file is not declared as JPEG or PNG. */
public class FileTypeNotAllowedException extends BusinessException {
    public FileTypeNotAllowedException() { super(ErrorCode.FILE_TYPE_NOT_ALLOWED); }
}
