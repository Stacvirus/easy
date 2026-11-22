package com.stac.documentdigitization.repositories;

import com.stac.documentdigitization.entities.Subject;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Every subject lookup used by a request includes its authenticated owner. */
public interface SubjectRepository extends JpaRepository<Subject, UUID> {
    List<Subject> findAllByOwnerIdOrderByNameAsc(UUID ownerId);
    Optional<Subject> findByIdAndOwnerId(UUID id, UUID ownerId);
    boolean existsByOwnerIdAndNameIgnoreCase(UUID ownerId, String name);
}
