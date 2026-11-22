package com.stac.documentdigitization.mappers;

import com.stac.documentdigitization.dtos.responses.DocumentExportResponseDto;
import com.stac.documentdigitization.entities.DocumentExport;
import org.springframework.stereotype.Component;

/** Keeps storage keys out of export responses. */
@Component
public class DocumentExportMapper {
    public DocumentExportResponseDto toResponse(DocumentExport export) {
        return new DocumentExportResponseDto(export.getId(), export.getJobId(), export.getSizeBytes(),
                export.getCreatedAt());
    }
}
