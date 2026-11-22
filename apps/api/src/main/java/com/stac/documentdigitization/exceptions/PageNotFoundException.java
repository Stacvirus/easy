package com.stac.documentdigitization.exceptions;

/** Requested page is absent from an owned document. */
public class PageNotFoundException extends BusinessException {
    public PageNotFoundException() { super(ErrorCode.PAGE_NOT_FOUND); }
}
