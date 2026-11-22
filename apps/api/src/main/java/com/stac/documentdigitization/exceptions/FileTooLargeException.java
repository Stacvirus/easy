package com.stac.documentdigitization.exceptions;

/** Uploaded image exceeds the per-file size limit. */
public class FileTooLargeException extends BusinessException {
    public FileTooLargeException() { super(ErrorCode.FILE_TOO_LARGE); }
}
