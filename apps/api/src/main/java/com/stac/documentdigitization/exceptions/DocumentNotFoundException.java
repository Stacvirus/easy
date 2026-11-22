package com.stac.documentdigitization.exceptions;

/** Requested document is absent or does not belong to the current user. */
public class DocumentNotFoundException extends BusinessException {
    public DocumentNotFoundException() {
        super(ErrorCode.DOCUMENT_NOT_FOUND);
    }
}
