package com.stac.documentdigitization.configurations;

import com.stac.documentdigitization.services.ObjectCleanupService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Retries durable object removals after successful page and document deletes. */
@Component
@EnableScheduling
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.storage.cleanup-enabled", havingValue = "true", matchIfMissing = true)
public class ObjectCleanupScheduler {
    private final ObjectCleanupService cleanup;

    @Scheduled(initialDelay = 10000, fixedDelay = 10000)
    public void run() {
        cleanup.processPending();
    }
}
