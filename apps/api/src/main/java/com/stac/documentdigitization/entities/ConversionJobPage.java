package com.stac.documentdigitization.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Immutable page ID, object key and position captured when a job is submitted. */
@Entity
@Table(name = "conversion_job_page")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConversionJobPage {
    @Id
    private UUID id;
    @Column(name = "job_id", nullable = false)
    private UUID jobId;
    @Column(nullable = false)
    private int position;
    @Column(name = "page_id", nullable = false)
    private UUID pageId;
    @Column(name = "object_key", nullable = false, length = 512)
    private String objectKey;
    @Column(name = "content_type", nullable = false, length = 32)
    private String contentType;

    public ConversionJobPage(UUID jobId, DocumentPage page) {
        this.id = UUID.randomUUID();
        this.jobId = jobId;
        this.position = page.getPosition();
        this.pageId = page.getId();
        this.objectKey = page.getObjectKey();
        this.contentType = page.getContentType();
    }
}
