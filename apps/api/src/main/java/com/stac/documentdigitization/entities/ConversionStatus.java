package com.stac.documentdigitization.entities;

/** Persisted lifecycle of an asynchronous conversion request. */
public enum ConversionStatus {
    PENDING, PROCESSING, COMPLETED, FAILED
}
