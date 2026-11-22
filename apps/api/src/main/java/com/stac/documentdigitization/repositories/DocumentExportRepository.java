package com.stac.documentdigitization.repositories;

import com.stac.documentdigitization.entities.DocumentExport;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Lists immutable exports through an owned document. */
public interface DocumentExportRepository extends JpaRepository<DocumentExport, UUID> {
    List<DocumentExport> findAllByDocumentIdOrderByCreatedAtDesc(UUID documentId);
    Optional<DocumentExport> findByIdAndDocumentId(UUID id, UUID documentId);
}
