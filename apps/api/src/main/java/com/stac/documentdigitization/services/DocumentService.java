package com.stac.documentdigitization.services;

import com.stac.documentdigitization.dtos.requests.DocumentCreateRequestDto;
import com.stac.documentdigitization.dtos.responses.DocumentResponseDto;
import com.stac.documentdigitization.entities.Document;
import com.stac.documentdigitization.exceptions.DocumentNotFoundException;
import com.stac.documentdigitization.exceptions.ConversionInProgressException;
import com.stac.documentdigitization.exceptions.SubjectNotFoundException;
import com.stac.documentdigitization.mappers.DocumentMapper;
import com.stac.documentdigitization.repositories.DocumentRepository;
import com.stac.documentdigitization.repositories.DocumentPageRepository;
import com.stac.documentdigitization.repositories.ConversionJobRepository;
import com.stac.documentdigitization.repositories.DocumentExportRepository;
import com.stac.documentdigitization.entities.ConversionStatus;
import com.stac.documentdigitization.repositories.SubjectRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Creates and retrieves owner-scoped document metadata before pages are uploaded. */
@Service
@RequiredArgsConstructor
public class DocumentService {
    private final DocumentRepository documents;
    private final SubjectRepository subjects;
    private final DocumentPageRepository pages;
    private final ConversionJobRepository jobs;
    private final DocumentExportRepository exports;
    private final ObjectCleanupService cleanup;
    private final DocumentMapper mapper;

    /** Attach a subject only when the authenticated user owns it. */
    @Transactional
    public DocumentResponseDto create(UUID ownerId, DocumentCreateRequestDto request) {
        if (request.subjectId() != null && subjects.findByIdAndOwnerId(request.subjectId(), ownerId).isEmpty()) {
            throw new SubjectNotFoundException();
        }
        return mapper.toResponse(documents.save(new Document(ownerId, request.subjectId(),
                request.title().trim(), request.documentDate())));
    }

    @Transactional(readOnly = true)
    public DocumentResponseDto get(UUID ownerId, UUID id) {
        return mapper.toResponse(findOwned(ownerId, id));
    }

    /** Filter the owner's library by optional subject and logical document date. */
    @Transactional(readOnly = true)
    public List<DocumentResponseDto> list(UUID ownerId, UUID subjectId, LocalDate date) {
        List<Document> found;
        if (subjectId != null && date != null) {
            found = documents.findAllByOwnerIdAndSubjectIdAndDocumentDateOrderByCreatedAtDesc(ownerId, subjectId, date);
        } else if (subjectId != null) {
            found = documents.findAllByOwnerIdAndSubjectIdOrderByCreatedAtDesc(ownerId, subjectId);
        } else if (date != null) {
            found = documents.findAllByOwnerIdAndDocumentDateOrderByCreatedAtDesc(ownerId, date);
        } else {
            found = documents.findAllByOwnerIdOrderByCreatedAtDesc(ownerId);
        }
        return found.stream().map(mapper::toResponse).toList();
    }

    /** Delete only a document owned by the authenticated user. */
    @Transactional
    public void delete(UUID ownerId, UUID id) {
        Document document = documents.findOwnedForUpdate(id, ownerId).orElseThrow(DocumentNotFoundException::new);
        if (jobs.existsByDocumentIdAndStatusIn(id, List.of(ConversionStatus.PENDING, ConversionStatus.PROCESSING))) {
            throw new ConversionInProgressException();
        }
        pages.findAllByDocumentIdOrderByPositionAsc(id)
                .forEach(page -> cleanup.queue(page.getObjectKey()));
        exports.findAllByDocumentIdOrderByCreatedAtDesc(id)
                .forEach(export -> cleanup.queue(export.getObjectKey()));
        documents.delete(document);
    }

    private Document findOwned(UUID ownerId, UUID id) {
        return documents.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(DocumentNotFoundException::new);
    }
}
