package com.stac.documentdigitization.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Immutable reference to one completed DOCX stored in MinIO. */
@Entity
@Table(name = "document_export")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DocumentExport {
    @Id
    private UUID id;
    @Column(name = "job_id", nullable = false)
    private UUID jobId;
    @Column(name = "document_id", nullable = false)
    private UUID documentId;
    @Column(name = "object_key", nullable = false, length = 512)
    private String objectKey;
    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public DocumentExport(UUID jobId, UUID documentId, String objectKey, long sizeBytes) {
        this.id = UUID.randomUUID();
        this.jobId = jobId;
        this.documentId = documentId;
        this.objectKey = objectKey;
        this.sizeBytes = sizeBytes;
        this.createdAt = Instant.now();
    }
}
