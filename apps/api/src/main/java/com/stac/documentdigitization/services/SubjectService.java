package com.stac.documentdigitization.services;

import com.stac.documentdigitization.dtos.responses.SubjectResponseDto;
import com.stac.documentdigitization.entities.Subject;
import com.stac.documentdigitization.exceptions.SubjectAlreadyExistsException;
import com.stac.documentdigitization.exceptions.SubjectInUseException;
import com.stac.documentdigitization.exceptions.SubjectNotFoundException;
import com.stac.documentdigitization.mappers.SubjectMapper;
import com.stac.documentdigitization.repositories.DocumentRepository;
import com.stac.documentdigitization.repositories.SubjectRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Manages only the authenticated user's subjects and prevents deleting referenced subjects. */
@Service
@RequiredArgsConstructor
public class SubjectService {
    private final SubjectRepository subjects;
    private final DocumentRepository documents;
    private final SubjectMapper mapper;

    /** Create a subject whose name is unique for this owner, ignoring case. */
    @Transactional
    public SubjectResponseDto create(UUID ownerId, String name) {
        String normalized = name.trim();
        if (subjects.existsByOwnerIdAndNameIgnoreCase(ownerId, normalized)) {
            throw duplicateName();
        }
        return mapper.toResponse(subjects.saveAndFlush(new Subject(ownerId, normalized)));
    }

    /** Return only subjects owned by the authenticated user. */
    @Transactional(readOnly = true)
    public List<SubjectResponseDto> list(UUID ownerId) {
        return subjects.findAllByOwnerIdOrderByNameAsc(ownerId).stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public SubjectResponseDto get(UUID ownerId, UUID id) {
        return mapper.toResponse(findOwned(ownerId, id));
    }

    /** Rename an owned subject while preserving its stable identifier. */
    @Transactional
    public SubjectResponseDto rename(UUID ownerId, UUID id, String name) {
        Subject subject = findOwned(ownerId, id);
        String normalized = name.trim();
        if (!subject.getName().equalsIgnoreCase(normalized)
                && subjects.existsByOwnerIdAndNameIgnoreCase(ownerId, normalized)) {
            throw duplicateName();
        }
        subject.rename(normalized);
        return mapper.toResponse(subject);
    }

    /** Reject deletion when a document still references the subject. */
    @Transactional
    public void delete(UUID ownerId, UUID id) {
        Subject subject = findOwned(ownerId, id);
        if (documents.existsByOwnerIdAndSubjectId(ownerId, id)) {
            throw new SubjectInUseException();
        }
        subjects.delete(subject);
    }

    private Subject findOwned(UUID ownerId, UUID id) {
        return subjects.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(SubjectNotFoundException::new);
    }

    private static SubjectAlreadyExistsException duplicateName() {
        return new SubjectAlreadyExistsException();
    }
}
