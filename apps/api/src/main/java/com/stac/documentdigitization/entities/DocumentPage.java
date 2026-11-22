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

/** One stored original image with a stable identity and explicit one-based position. */
@Entity
@Table(name = "document_page")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DocumentPage {
    @Id
    private UUID id;
    @Column(name = "document_id", nullable = false)
    private UUID documentId;
    @Column(nullable = false)
    private int position;
    @Column(name = "object_key", nullable = false, length = 512)
    private String objectKey;
    @Column(name = "content_type", nullable = false, length = 32)
    private String contentType;
    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;
    @Column(name = "uploaded_at", nullable = false)
    private Instant uploadedAt;

    public DocumentPage(UUID id, UUID documentId, int position, String objectKey,
                        String contentType, long sizeBytes) {
        this.id = id;
        this.documentId = documentId;
        this.position = position;
        this.objectKey = objectKey;
        this.contentType = contentType;
        this.sizeBytes = sizeBytes;
        this.uploadedAt = Instant.now();
    }

    public void moveTo(int position) {
        this.position = position;
    }
}
