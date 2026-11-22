package com.stac.documentdigitization.mappers;

import com.stac.documentdigitization.dtos.responses.SubjectResponseDto;
import com.stac.documentdigitization.entities.Subject;
import org.springframework.stereotype.Component;

/** Converts subject entities to their public API representation. */
@Component
public class SubjectMapper {
    public SubjectResponseDto toResponse(Subject subject) {
        return new SubjectResponseDto(subject.getId(), subject.getName(), subject.getCreatedAt());
    }
}
