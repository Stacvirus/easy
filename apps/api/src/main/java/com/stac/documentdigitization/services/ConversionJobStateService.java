package com.stac.documentdigitization.services;

import com.stac.documentdigitization.entities.ConversionJob;
import com.stac.documentdigitization.entities.ConversionJobPage;
import com.stac.documentdigitization.entities.ConversionStatus;
import com.stac.documentdigitization.entities.DocumentExport;
import com.stac.documentdigitization.exceptions.ErrorCode;
import com.stac.documentdigitization.repositories.ConversionJobPageRepository;
import com.stac.documentdigitization.repositories.ConversionJobRepository;
import com.stac.documentdigitization.repositories.DocumentExportRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Small transactions around durable job claims and terminal state changes. */
@Service
@RequiredArgsConstructor
public class ConversionJobStateService {
    private final ConversionJobRepository jobs;
    private final ConversionJobPageRepository pages;
    private final DocumentExportRepository exports;

    /** A single API worker requeues work interrupted by its previous process. */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void requeueInterrupted() {
        jobs.requeueInterrupted(ConversionStatus.PENDING, ConversionStatus.PROCESSING);
    }

    @Transactional
    public JobClaim claimNext() {
        ConversionJob job = jobs.findNextPendingForUpdate().orElse(null);
        if (job == null) {
            return null;
        }
        job.claim();
        List<PageSnapshot> snapshot = pages.findAllByJobIdOrderByPositionAsc(job.getId()).stream()
                .map(PageSnapshot::from).toList();
        return new JobClaim(job.getId(), job.getOwnerId(), job.getDocumentId(), job.getLanguage(), snapshot);
    }

    @Transactional
    public void complete(UUID jobId, String objectKey, long sizeBytes) {
        ConversionJob job = jobs.findById(jobId).orElseThrow();
        if (job.getStatus() != ConversionStatus.PROCESSING) {
            throw new IllegalStateException("Job is not processing");
        }
        exports.save(new DocumentExport(jobId, job.getDocumentId(), objectKey, sizeBytes));
        job.complete();
    }

    @Transactional
    public void fail(UUID jobId, ErrorCode code) {
        ConversionJob job = jobs.findById(jobId).orElseThrow();
        if (job.getStatus() == ConversionStatus.PROCESSING) {
            job.fail(code.name());
        }
    }

    public record JobClaim(UUID jobId, UUID ownerId, UUID documentId, String language,
                           List<PageSnapshot> pages) {}

    public record PageSnapshot(UUID pageId, int position, String objectKey, String contentType) {
        static PageSnapshot from(ConversionJobPage page) {
            return new PageSnapshot(page.getPageId(), page.getPosition(), page.getObjectKey(), page.getContentType());
        }
    }
}
