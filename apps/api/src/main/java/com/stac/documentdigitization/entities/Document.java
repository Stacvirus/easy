package com.stac.documentdigitization.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Metadata for a future image upload and export, separate from stored file bytes. */
@Entity
@Table(name = "document")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Document {
    @Id
    private UUID id;
    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;
    @Column(name = "subject_id")
    private UUID subjectId;
    @Column(nullable = false, length = 200)
    private String title;
    @Column(name = "document_date", nullable = false)
    private LocalDate documentDate;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public Document(UUID ownerId, UUID subjectId, String title, LocalDate documentDate) {
        this.id = UUID.randomUUID();
        this.ownerId = ownerId;
        this.subjectId = subjectId;
        this.title = title;
        this.documentDate = documentDate;
        this.createdAt = Instant.now();
    }

}
