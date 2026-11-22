package com.stac.documentdigitization.exceptions;

/** An owner already has a subject with this name. */
public class SubjectAlreadyExistsException extends BusinessException {
    public SubjectAlreadyExistsException() {
        super(ErrorCode.SUBJECT_EXISTS);
    }
}
