package com.stac.documentdigitization.exceptions;

/** Reorder list does not contain every current page exactly once. */
public class PageOrderInvalidException extends BusinessException {
    public PageOrderInvalidException() { super(ErrorCode.PAGE_ORDER_INVALID); }
}
