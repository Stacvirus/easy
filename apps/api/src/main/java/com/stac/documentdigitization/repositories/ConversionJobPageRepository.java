package com.stac.documentdigitization.repositories;

import com.stac.documentdigitization.entities.ConversionJobPage;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Reads the order captured for a conversion, independent of later page edits. */
public interface ConversionJobPageRepository extends JpaRepository<ConversionJobPage, UUID> {
    List<ConversionJobPage> findAllByJobIdOrderByPositionAsc(UUID jobId);
}
