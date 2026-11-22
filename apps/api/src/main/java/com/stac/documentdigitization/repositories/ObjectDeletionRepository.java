package com.stac.documentdigitization.repositories;

import com.stac.documentdigitization.entities.ObjectDeletion;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Durable cleanup queue for objects whose metadata has been removed. */
public interface ObjectDeletionRepository extends JpaRepository<ObjectDeletion, UUID> {
    List<ObjectDeletion> findTop20ByOrderByCreatedAtAsc();
}
