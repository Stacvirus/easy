package com.stac.documentdigitization.repositories;

import com.stac.documentdigitization.entities.ConversionJob;
import com.stac.documentdigitization.entities.ConversionStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Claims persisted work and scopes job lookups to their owner. */
public interface ConversionJobRepository extends JpaRepository<ConversionJob, UUID> {
    Optional<ConversionJob> findByIdAndOwnerId(UUID id, UUID ownerId);
    List<ConversionJob> findAllByDocumentIdAndOwnerIdOrderByCreatedAtDesc(UUID documentId, UUID ownerId);
    Optional<ConversionJob> findFirstByDocumentIdAndStatusInOrderByCreatedAtAsc(
            UUID documentId, Collection<ConversionStatus> statuses);
    boolean existsByDocumentIdAndStatusIn(UUID documentId, Collection<ConversionStatus> statuses);

    @Query(value = "SELECT * FROM conversion_job WHERE status = 'PENDING' "
            + "ORDER BY created_at, id LIMIT 1 FOR UPDATE SKIP LOCKED", nativeQuery = true)
    Optional<ConversionJob> findNextPendingForUpdate();

    @Modifying
    @Query("update ConversionJob j set j.status = :pending, j.startedAt = null, "
            + "j.finishedAt = null, j.failureCode = null where j.status = :processing")
    int requeueInterrupted(@Param("pending") ConversionStatus pending,
                           @Param("processing") ConversionStatus processing);
}
