package com.stac.documentdigitization.repositories;

import com.stac.documentdigitization.entities.DocumentPage;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Pages are always retrieved through an owned document. */
public interface DocumentPageRepository extends JpaRepository<DocumentPage, UUID> {
    List<DocumentPage> findAllByDocumentIdOrderByPositionAsc(UUID documentId);
    Optional<DocumentPage> findByIdAndDocumentId(UUID id, UUID documentId);
}
