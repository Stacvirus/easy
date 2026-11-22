package com.stac.documentdigitization.services.interfaces;

/** Private object storage; callers supply only server-generated keys. */
public interface StorageService {
    void put(String key, byte[] content, String contentType);
    byte[] get(String key);
    void delete(String key);
}
