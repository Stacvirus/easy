package com.stac.documentdigitization.services;

import com.stac.documentdigitization.clients.ocr.RecognizedPageDto;
import com.stac.documentdigitization.exceptions.ConversionTimeoutException;
import com.stac.documentdigitization.exceptions.ErrorCode;
import com.stac.documentdigitization.exceptions.OcrProcessingException;
import com.stac.documentdigitization.exceptions.OcrUnavailableException;
import com.stac.documentdigitization.exceptions.StorageUnavailableException;
import com.stac.documentdigitization.services.ConversionJobStateService.JobClaim;
import com.stac.documentdigitization.services.interfaces.OcrServiceClient;
import com.stac.documentdigitization.services.interfaces.StorageService;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** Runs one persisted job at a time outside the request and database transactions. */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConversionProcessor {
    private static final Duration MAX_DURATION = Duration.ofMinutes(30);
    private static final String DOCX_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

    private final ConversionJobStateService states;
    private final StorageService storage;
    private final OcrServiceClient ocr;
    private final ObjectCleanupService cleanup;

    /** @return true when a pending job was claimed, including jobs that ended in failure */
    public boolean processNext() {
        JobClaim claim = states.claimNext();
        if (claim == null) {
            return false;
        }
        String exportKey = "users/" + claim.ownerId() + "/documents/" + claim.documentId()
                + "/exports/" + claim.jobId() + ".docx";
        boolean attemptedExportPut = false;
        try {
            Instant deadline = Instant.now().plus(MAX_DURATION);
            List<RecognizedPageDto> recognized = new ArrayList<>();
            for (var page : claim.pages()) {
                checkDeadline(deadline);
                byte[] source = storage.get(page.objectKey());
                recognized.add(new RecognizedPageDto(page.pageId(), page.position(),
                        ocr.recognize(source, page.contentType(), claim.language())));
            }
            checkDeadline(deadline);
            byte[] docx = ocr.renderDocx(recognized);
            checkDeadline(deadline);
            attemptedExportPut = true;
            storage.put(exportKey, docx, DOCX_CONTENT_TYPE);
            states.complete(claim.jobId(), exportKey, docx.length);
        } catch (RuntimeException error) {
            if (attemptedExportPut) {
                try {
                    cleanup.queueAfterFailure(List.of(exportKey));
                } catch (RuntimeException queueError) {
                    log.error("Could not queue failed export upload for job {}", claim.jobId(), queueError);
                }
            }
            ErrorCode code = failureCode(error);
            log.warn("Conversion job {} failed with {}", claim.jobId(), code);
            states.fail(claim.jobId(), code);
        }
        return true;
    }

    private static void checkDeadline(Instant deadline) {
        if (Instant.now().isAfter(deadline)) {
            throw new ConversionTimeoutException();
        }
    }

    private static ErrorCode failureCode(RuntimeException error) {
        if (error instanceof StorageUnavailableException) return ErrorCode.STORAGE_UNAVAILABLE;
        if (error instanceof OcrUnavailableException) return ErrorCode.OCR_UNAVAILABLE;
        if (error instanceof OcrProcessingException) return ErrorCode.OCR_FAILED;
        if (error instanceof ConversionTimeoutException) return ErrorCode.CONVERSION_TIMEOUT;
        return ErrorCode.INTERNAL_ERROR;
    }
}
