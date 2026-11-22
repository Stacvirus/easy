package com.stac.documentdigitization.mappers;

import com.stac.documentdigitization.dtos.responses.ConversionJobResponseDto;
import com.stac.documentdigitization.entities.ConversionJob;
import com.stac.documentdigitization.exceptions.ErrorCode;
import com.stac.documentdigitization.services.I18nService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Adds a localized failure message to persisted job metadata. */
@Component
@RequiredArgsConstructor
public class ConversionJobMapper {
    private final I18nService i18n;

    public ConversionJobResponseDto toResponse(ConversionJob job) {
        String failureMessage = job.getFailureCode() == null ? null
                : i18n.message(ErrorCode.valueOf(job.getFailureCode()).getMessageKey());
        return new ConversionJobResponseDto(job.getId(), job.getDocumentId(), job.getStatus(),
                job.getLanguage(), job.getCreatedAt(), job.getStartedAt(), job.getFinishedAt(),
                job.getFailureCode(), failureMessage);
    }
}
