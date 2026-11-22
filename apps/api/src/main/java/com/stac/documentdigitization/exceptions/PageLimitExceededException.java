package com.stac.documentdigitization.exceptions;

/** Upload would exceed the maximum pages for one document. */
public class PageLimitExceededException extends BusinessException {
    public PageLimitExceededException() { super(ErrorCode.PAGE_LIMIT_EXCEEDED); }
}
