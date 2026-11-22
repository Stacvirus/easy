package com.stac.documentdigitization.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Durable conversion state and the owner of a fixed page snapshot. */
@Entity
@Table(name = "conversion_job")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConversionJob {
    @Id
    private UUID id;
    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;
    @Column(name = "document_id", nullable = false)
    private UUID documentId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ConversionStatus status;
    @Column(nullable = false, length = 2)
    private String language;
    @Column(nullable = false)
    private int attempts;
    @Column(name = "failure_code", length = 40)
    private String failureCode;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @Column(name = "started_at")
    private Instant startedAt;
    @Column(name = "finished_at")
    private Instant finishedAt;

    public ConversionJob(UUID ownerId, UUID documentId, String language) {
        this.id = UUID.randomUUID();
        this.ownerId = ownerId;
        this.documentId = documentId;
        this.language = language;
        this.status = ConversionStatus.PENDING;
        this.createdAt = Instant.now();
    }

    public void claim() {
        status = ConversionStatus.PROCESSING;
        attempts++;
        startedAt = Instant.now();
        finishedAt = null;
        failureCode = null;
    }

    public void complete() {
        status = ConversionStatus.COMPLETED;
        finishedAt = Instant.now();
    }

    public void fail(String code) {
        status = ConversionStatus.FAILED;
        failureCode = code;
        finishedAt = Instant.now();
    }
}
