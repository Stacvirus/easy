package com.stac.documentdigitization.mappers;

import com.stac.documentdigitization.dtos.responses.DocumentResponseDto;
import com.stac.documentdigitization.entities.Document;
import org.springframework.stereotype.Component;

/** Converts document metadata without exposing storage internals. */
@Component
public class DocumentMapper {
    public DocumentResponseDto toResponse(Document document) {
        return new DocumentResponseDto(document.getId(), document.getTitle(), document.getDocumentDate(),
                document.getSubjectId(), document.getCreatedAt());
    }
}
