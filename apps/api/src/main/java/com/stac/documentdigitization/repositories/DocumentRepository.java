package com.stac.documentdigitization.repositories;

import com.stac.documentdigitization.entities.Document;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

/** Owner-scoped document queries and library filters. */
public interface DocumentRepository extends JpaRepository<Document, UUID> {
    Optional<Document> findByIdAndOwnerId(UUID id, UUID ownerId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from Document d where d.id = :id and d.ownerId = :ownerId")
    Optional<Document> findOwnedForUpdate(@Param("id") UUID id, @Param("ownerId") UUID ownerId);
    List<Document> findAllByOwnerIdOrderByCreatedAtDesc(UUID ownerId);
    List<Document> findAllByOwnerIdAndSubjectIdOrderByCreatedAtDesc(UUID ownerId, UUID subjectId);
    List<Document> findAllByOwnerIdAndDocumentDateOrderByCreatedAtDesc(UUID ownerId, LocalDate date);
    List<Document> findAllByOwnerIdAndSubjectIdAndDocumentDateOrderByCreatedAtDesc(UUID ownerId, UUID subjectId, LocalDate date);
    boolean existsByOwnerIdAndSubjectId(UUID ownerId, UUID subjectId);
}
