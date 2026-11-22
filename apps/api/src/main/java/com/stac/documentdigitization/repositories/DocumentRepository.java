package com.stac.documentdigitization.repositories;

import com.stac.documentdigitization.entities.Document;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Owner-scoped document queries and library filters. */
public interface DocumentRepository extends JpaRepository<Document, UUID> {
    Optional<Document> findByIdAndOwnerId(UUID id, UUID ownerId);
    List<Document> findAllByOwnerIdOrderByCreatedAtDesc(UUID ownerId);
    List<Document> findAllByOwnerIdAndSubjectIdOrderByCreatedAtDesc(UUID ownerId, UUID subjectId);
    List<Document> findAllByOwnerIdAndDocumentDateOrderByCreatedAtDesc(UUID ownerId, LocalDate date);
    List<Document> findAllByOwnerIdAndSubjectIdAndDocumentDateOrderByCreatedAtDesc(UUID ownerId, UUID subjectId, LocalDate date);
    boolean existsByOwnerIdAndSubjectId(UUID ownerId, UUID subjectId);
}
