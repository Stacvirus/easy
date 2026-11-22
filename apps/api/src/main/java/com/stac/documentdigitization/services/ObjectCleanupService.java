package com.stac.documentdigitization.services;

import com.stac.documentdigitization.entities.ObjectDeletion;
import com.stac.documentdigitization.exceptions.StorageUnavailableException;
import com.stac.documentdigitization.repositories.ObjectDeletionRepository;
import com.stac.documentdigitization.services.interfaces.StorageService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Persists object deletions and retries them independently of page metadata. */
@Slf4j
@Service
@RequiredArgsConstructor
public class ObjectCleanupService {
    private final ObjectDeletionRepository deletions;
    private final StorageService storage;

    /** Called inside the same transaction that removes page metadata. */
    public void queue(String key) {
        deletions.save(new ObjectDeletion(key));
    }

    /** A failed upload has rolled back; record attempted object keys in a new transaction. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void queueAfterFailure(List<String> keys) {
        deletions.saveAll(keys.stream().map(ObjectDeletion::new).toList());
    }

    /** Keep failed deletions queued for the next run. S3 deletion is idempotent. */
    @Transactional
    public void processPending() {
        for (ObjectDeletion deletion : deletions.findTop20ByOrderByCreatedAtAsc()) {
            try {
                storage.delete(deletion.getObjectKey());
                deletions.delete(deletion);
            } catch (StorageUnavailableException error) {
                log.warn("Object deletion will be retried for key {}", deletion.getObjectKey());
            }
        }
    }
}
