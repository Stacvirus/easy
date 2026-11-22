package com.stac.documentdigitization.exceptions;

/** A conversion cannot start before the document has images. */
public class DocumentHasNoPagesException extends BusinessException {
    public DocumentHasNoPagesException() { super(ErrorCode.DOCUMENT_HAS_NO_PAGES); }
}
