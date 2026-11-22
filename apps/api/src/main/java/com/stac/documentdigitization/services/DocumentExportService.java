package com.stac.documentdigitization.services;

import com.stac.documentdigitization.dtos.responses.DocumentExportResponseDto;
import com.stac.documentdigitization.entities.DocumentExport;
import com.stac.documentdigitization.exceptions.DocumentNotFoundException;
import com.stac.documentdigitization.exceptions.ExportNotFoundException;
import com.stac.documentdigitization.mappers.DocumentExportMapper;
import com.stac.documentdigitization.repositories.DocumentExportRepository;
import com.stac.documentdigitization.repositories.DocumentRepository;
import com.stac.documentdigitization.services.interfaces.StorageService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Lists and downloads exports only through an owned document. */
@Service
@RequiredArgsConstructor
public class DocumentExportService {
    private final DocumentRepository documents;
    private final DocumentExportRepository exports;
    private final DocumentExportMapper mapper;
    private final StorageService storage;

    @Transactional(readOnly = true)
    public List<DocumentExportResponseDto> list(UUID ownerId, UUID documentId) {
        documents.findByIdAndOwnerId(documentId, ownerId).orElseThrow(DocumentNotFoundException::new);
        return exports.findAllByDocumentIdOrderByCreatedAtDesc(documentId)
                .stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ExportContent download(UUID ownerId, UUID documentId, UUID exportId) {
        documents.findByIdAndOwnerId(documentId, ownerId).orElseThrow(DocumentNotFoundException::new);
        DocumentExport export = exports.findByIdAndDocumentId(exportId, documentId)
                .orElseThrow(ExportNotFoundException::new);
        return new ExportContent(storage.get(export.getObjectKey()), export.getId());
    }

    public record ExportContent(byte[] bytes, UUID exportId) {}
}
