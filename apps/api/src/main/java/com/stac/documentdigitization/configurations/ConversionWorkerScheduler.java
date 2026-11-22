package com.stac.documentdigitization.configurations;

import com.stac.documentdigitization.services.ConversionProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Polls for one persisted conversion at a time without blocking HTTP requests. */
@Component
@EnableScheduling
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.conversion.worker-enabled", havingValue = "true", matchIfMissing = true)
public class ConversionWorkerScheduler {
    private final ConversionProcessor processor;

    @Scheduled(initialDelay = 3000, fixedDelay = 2000)
    public void run() {
        processor.processNext();
    }
}
