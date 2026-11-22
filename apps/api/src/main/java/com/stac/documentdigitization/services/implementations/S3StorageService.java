package com.stac.documentdigitization.services.implementations;

import com.stac.documentdigitization.exceptions.StorageUnavailableException;
import com.stac.documentdigitization.properties.StorageProperties;
import com.stac.documentdigitization.services.interfaces.StorageService;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

/** Stores original images in a private MinIO bucket. */
@Slf4j
@Service
@RequiredArgsConstructor
public class S3StorageService implements StorageService {
    private final S3Client s3;
    private final StorageProperties properties;
    private final AtomicBoolean bucketReady = new AtomicBoolean();

    @Override
    public void put(String key, byte[] content, String contentType) {
        ensureBucket();
        try {
            s3.putObject(PutObjectRequest.builder().bucket(properties.bucket()).key(key)
                    .contentType(contentType).build(), RequestBody.fromBytes(content));
        } catch (SdkException error) {
            log.error("Object upload failed for key {}", key, error);
            throw new StorageUnavailableException();
        }
    }

    @Override
    public byte[] get(String key) {
        try {
            return s3.getObjectAsBytes(GetObjectRequest.builder().bucket(properties.bucket()).key(key).build())
                    .asByteArray();
        } catch (SdkException error) {
            log.error("Object read failed for key {}", key, error);
            throw new StorageUnavailableException();
        }
    }

    @Override
    public void delete(String key) {
        try {
            s3.deleteObject(DeleteObjectRequest.builder().bucket(properties.bucket()).key(key).build());
        } catch (S3Exception error) {
            if (error.statusCode() == 404) {
                return;
            }
            log.error("Object deletion failed for key {}", key, error);
            throw new StorageUnavailableException();
        } catch (SdkException error) {
            log.error("Object deletion failed for key {}", key, error);
            throw new StorageUnavailableException();
        }
    }

    /** Create the local bucket on first upload, without requiring a manual console step. */
    private void ensureBucket() {
        if (bucketReady.get()) {
            return;
        }
        synchronized (bucketReady) {
            if (bucketReady.get()) {
                return;
            }
            try {
                s3.headBucket(HeadBucketRequest.builder().bucket(properties.bucket()).build());
            } catch (S3Exception error) {
                if (error.statusCode() != 404) {
                    log.error("Bucket check failed for {}", properties.bucket(), error);
                    throw new StorageUnavailableException();
                }
                try {
                    s3.createBucket(CreateBucketRequest.builder().bucket(properties.bucket()).build());
                } catch (S3Exception createError) {
                    if (createError.statusCode() != 409) {
                        log.error("Bucket creation failed for {}", properties.bucket(), createError);
                        throw new StorageUnavailableException();
                    }
                } catch (SdkException createError) {
                    log.error("Bucket creation failed for {}", properties.bucket(), createError);
                    throw new StorageUnavailableException();
                }
            } catch (SdkException error) {
                log.error("Bucket check failed for {}", properties.bucket(), error);
                throw new StorageUnavailableException();
            }
            bucketReady.set(true);
        }
    }
}
