package com.stac.documentdigitization.services;

import com.stac.documentdigitization.dtos.responses.ConversionJobResponseDto;
import com.stac.documentdigitization.entities.ConversionJob;
import com.stac.documentdigitization.entities.ConversionJobPage;
import com.stac.documentdigitization.entities.ConversionStatus;
import com.stac.documentdigitization.entities.DocumentPage;
import com.stac.documentdigitization.exceptions.ConversionNotFoundException;
import com.stac.documentdigitization.exceptions.DocumentHasNoPagesException;
import com.stac.documentdigitization.exceptions.DocumentNotFoundException;
import com.stac.documentdigitization.mappers.ConversionJobMapper;
import com.stac.documentdigitization.repositories.ConversionJobPageRepository;
import com.stac.documentdigitization.repositories.ConversionJobRepository;
import com.stac.documentdigitization.repositories.DocumentPageRepository;
import com.stac.documentdigitization.repositories.DocumentRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Submits a durable conversion with a snapshot of the current page order. */
@Service
@RequiredArgsConstructor
public class ConversionJobService {
    private static final List<ConversionStatus> ACTIVE = List.of(ConversionStatus.PENDING, ConversionStatus.PROCESSING);

    private final DocumentRepository documents;
    private final DocumentPageRepository pages;
    private final ConversionJobRepository jobs;
    private final ConversionJobPageRepository snapshots;
    private final ConversionJobMapper mapper;

    /** Repeated submissions while active return the same job instead of creating duplicate work. */
    @Transactional
    public ConversionJobResponseDto submit(UUID ownerId, UUID documentId, String language) {
        documents.findOwnedForUpdate(documentId, ownerId).orElseThrow(DocumentNotFoundException::new);
        var active = jobs.findFirstByDocumentIdAndStatusInOrderByCreatedAtAsc(documentId, ACTIVE);
        if (active.isPresent()) {
            return mapper.toResponse(active.get());
        }
        List<DocumentPage> current = pages.findAllByDocumentIdOrderByPositionAsc(documentId);
        if (current.isEmpty()) {
            throw new DocumentHasNoPagesException();
        }
        ConversionJob job = jobs.save(new ConversionJob(ownerId, documentId, language));
        snapshots.saveAll(current.stream().map(page -> new ConversionJobPage(job.getId(), page)).toList());
        return mapper.toResponse(job);
    }

    @Transactional(readOnly = true)
    public ConversionJobResponseDto get(UUID ownerId, UUID jobId) {
        return mapper.toResponse(jobs.findByIdAndOwnerId(jobId, ownerId)
                .orElseThrow(ConversionNotFoundException::new));
    }

    @Transactional(readOnly = true)
    public List<ConversionJobResponseDto> list(UUID ownerId, UUID documentId) {
        documents.findByIdAndOwnerId(documentId, ownerId).orElseThrow(DocumentNotFoundException::new);
        return jobs.findAllByDocumentIdAndOwnerIdOrderByCreatedAtDesc(documentId, ownerId)
                .stream().map(mapper::toResponse).toList();
    }
}
