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

/** Durable request to remove an object after its page metadata is deleted. */
@Entity
@Table(name = "object_deletion")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ObjectDeletion {
    @Id
    private UUID id;
    @Column(name = "object_key", nullable = false, length = 512)
    private String objectKey;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public ObjectDeletion(String objectKey) {
        this.id = UUID.randomUUID();
        this.objectKey = objectKey;
        this.createdAt = Instant.now();
    }
}
