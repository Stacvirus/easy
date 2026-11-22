package com.stac.documentdigitization.exceptions;

/** MinIO could not complete an object operation. */
public class StorageUnavailableException extends BusinessException {
    public StorageUnavailableException() { super(ErrorCode.STORAGE_UNAVAILABLE); }
}
