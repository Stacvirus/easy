package com.stac.documentdigitization.services;

import com.stac.documentdigitization.dtos.responses.PageResponseDto;
import com.stac.documentdigitization.entities.DocumentPage;
import com.stac.documentdigitization.exceptions.DocumentNotFoundException;
import com.stac.documentdigitization.exceptions.FileEmptyException;
import com.stac.documentdigitization.exceptions.PageLimitExceededException;
import com.stac.documentdigitization.exceptions.PageNotFoundException;
import com.stac.documentdigitization.exceptions.PageOrderInvalidException;
import com.stac.documentdigitization.exceptions.ConversionInProgressException;
import com.stac.documentdigitization.entities.ConversionStatus;
import com.stac.documentdigitization.mappers.PageMapper;
import com.stac.documentdigitization.repositories.DocumentPageRepository;
import com.stac.documentdigitization.repositories.DocumentRepository;
import com.stac.documentdigitization.repositories.ConversionJobRepository;
import com.stac.documentdigitization.services.ImageUploadValidator.ValidatedImage;
import com.stac.documentdigitization.services.interfaces.StorageService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

/** Owns upload validation, durable page order and access to original images. */
@Slf4j
@Service
@RequiredArgsConstructor
public class PageService {
    private static final int MAX_PAGES = 50;

    private final DocumentRepository documents;
    private final DocumentPageRepository pages;
    private final ConversionJobRepository jobs;
    private final ImageUploadValidator validator;
    private final StorageService storage;
    private final ObjectCleanupService cleanup;
    private final PageMapper mapper;

    /** Validate every file before any object is stored; append under a document lock. */
    @Transactional
    public List<PageResponseDto> upload(UUID ownerId, UUID documentId, List<MultipartFile> files) {
        ownedForUpdate(ownerId, documentId);
        if (files == null || files.isEmpty()) {
            throw new FileEmptyException();
        }
        List<DocumentPage> current = pages.findAllByDocumentIdOrderByPositionAsc(documentId);
        if (current.size() + files.size() > MAX_PAGES) {
            throw new PageLimitExceededException();
        }
        List<ValidatedImage> images = files.stream().map(validator::validate).toList();
        List<String> attemptedKeys = new ArrayList<>();
        registerRollbackCleanup(attemptedKeys);
        List<DocumentPage> created = new ArrayList<>();
        for (ValidatedImage image : images) {
            UUID pageId = UUID.randomUUID();
            String key = "users/" + ownerId + "/documents/" + documentId + "/pages/" + pageId
                    + "." + image.extension();
            attemptedKeys.add(key);
            storage.put(key, image.bytes(), image.contentType());
            created.add(pages.save(new DocumentPage(pageId, documentId, current.size() + created.size() + 1,
                    key, image.contentType(), image.bytes().length)));
        }
        pages.flush();
        return created.stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<PageResponseDto> list(UUID ownerId, UUID documentId) {
        owned(ownerId, documentId);
        return pages.findAllByDocumentIdOrderByPositionAsc(documentId).stream().map(mapper::toResponse).toList();
    }

    /** Reorder only when every current page ID occurs exactly once. */
    @Transactional
    public List<PageResponseDto> reorder(UUID ownerId, UUID documentId, List<UUID> orderedIds) {
        ownedForUpdate(ownerId, documentId);
        List<DocumentPage> current = pages.findAllByDocumentIdOrderByPositionAsc(documentId);
        if (orderedIds == null || orderedIds.size() != current.size()
                || new HashSet<>(orderedIds).size() != current.size()) {
            throw new PageOrderInvalidException();
        }
        Map<UUID, DocumentPage> byId = new HashMap<>();
        current.forEach(page -> byId.put(page.getId(), page));
        if (!byId.keySet().equals(new HashSet<>(orderedIds))) {
            throw new PageOrderInvalidException();
        }
        List<PageResponseDto> reordered = new ArrayList<>();
        for (int index = 0; index < orderedIds.size(); index++) {
            DocumentPage page = byId.get(orderedIds.get(index));
            page.moveTo(index + 1);
            reordered.add(mapper.toResponse(page));
        }
        return reordered;
    }

    @Transactional(readOnly = true)
    public PageContent read(UUID ownerId, UUID documentId, UUID pageId) {
        owned(ownerId, documentId);
        DocumentPage page = pages.findByIdAndDocumentId(pageId, documentId)
                .orElseThrow(PageNotFoundException::new);
        return new PageContent(storage.get(page.getObjectKey()), page.getContentType());
    }

    /** Queue object deletion in the same transaction that removes page metadata. */
    @Transactional
    public void delete(UUID ownerId, UUID documentId, UUID pageId) {
        ownedForUpdate(ownerId, documentId);
        if (jobs.existsByDocumentIdAndStatusIn(documentId,
                List.of(ConversionStatus.PENDING, ConversionStatus.PROCESSING))) {
            throw new ConversionInProgressException();
        }
        List<DocumentPage> current = pages.findAllByDocumentIdOrderByPositionAsc(documentId);
        DocumentPage removed = current.stream().filter(page -> page.getId().equals(pageId)).findFirst()
                .orElseThrow(PageNotFoundException::new);
        cleanup.queue(removed.getObjectKey());
        pages.delete(removed);
        int position = 1;
        for (DocumentPage page : current) {
            if (!page.getId().equals(pageId)) {
                page.moveTo(position++);
            }
        }
    }

    private void owned(UUID ownerId, UUID documentId) {
        documents.findByIdAndOwnerId(documentId, ownerId).orElseThrow(DocumentNotFoundException::new);
    }

    private void ownedForUpdate(UUID ownerId, UUID documentId) {
        documents.findOwnedForUpdate(documentId, ownerId).orElseThrow(DocumentNotFoundException::new);
    }

    private void registerRollbackCleanup(List<String> attemptedKeys) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_ROLLED_BACK && !attemptedKeys.isEmpty()) {
                    try {
                        cleanup.queueAfterFailure(List.copyOf(attemptedKeys));
                    } catch (RuntimeException error) {
                        log.error("Could not queue cleanup for rolled-back page upload", error);
                    }
                }
            }
        });
    }

    public record PageContent(byte[] bytes, String contentType) {}
}
