package com.stac.documentdigitization.configurations.ocr;

import com.stac.documentdigitization.exceptions.OcrProcessingException;
import com.stac.documentdigitization.exceptions.OcrUnavailableException;
import com.stac.documentdigitization.properties.OcrProperties;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

/** Scoped to the OCR Feign client so its token and error mapping do not affect other clients. */
public class OcrFeignConfiguration {
    @Bean
    public RequestInterceptor ocrTokenInterceptor(OcrProperties properties) {
        return template -> template.header("X-Internal-Token", properties.token());
    }

    @Bean
    public ErrorDecoder ocrErrorDecoder() {
        return (method, response) -> response.status() >= 500 || response.status() == 401
                ? new OcrUnavailableException() : new OcrProcessingException();
    }
}
