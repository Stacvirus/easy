package com.stac.documentdigitization.mappers;

import com.stac.documentdigitization.dtos.responses.PageResponseDto;
import com.stac.documentdigitization.entities.DocumentPage;
import org.springframework.stereotype.Component;

/** Maps stored page metadata to the public response. */
@Component
public class PageMapper {
    public PageResponseDto toResponse(DocumentPage page) {
        return new PageResponseDto(page.getId(), page.getPosition(), page.getContentType(),
                page.getSizeBytes(), page.getUploadedAt());
    }
}
