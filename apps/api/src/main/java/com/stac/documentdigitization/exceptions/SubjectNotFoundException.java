package com.stac.documentdigitization.exceptions;

/** Requested subject is absent or does not belong to the current user. */
public class SubjectNotFoundException extends BusinessException {
    public SubjectNotFoundException() {
        super(ErrorCode.SUBJECT_NOT_FOUND);
    }
}
