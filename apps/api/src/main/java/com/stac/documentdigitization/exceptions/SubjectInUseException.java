package com.stac.documentdigitization.exceptions;

/** A subject still belongs to at least one document. */
public class SubjectInUseException extends BusinessException {
    public SubjectInUseException() {
        super(ErrorCode.SUBJECT_IN_USE);
    }
}
