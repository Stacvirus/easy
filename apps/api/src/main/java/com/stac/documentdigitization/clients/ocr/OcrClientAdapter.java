package com.stac.documentdigitization.clients.ocr;

import com.stac.documentdigitization.exceptions.OcrProcessingException;
import com.stac.documentdigitization.exceptions.OcrUnavailableException;
import com.stac.documentdigitization.services.interfaces.OcrServiceClient;
import feign.FeignException;
import feign.RetryableException;
import feign.codec.DecodeException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/** Validates successful OCR responses and translates transport failures for the conversion worker. */
@Component
@RequiredArgsConstructor
@Slf4j
public class OcrClientAdapter implements OcrServiceClient {
    private static final String DOCX_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    private final OcrFeignClient client;

    @Override
    public List<RecognizedLineDto> recognize(byte[] image, String contentType, String language) {
        try {
            RecognizeResponseDto result = "image/png".equals(contentType)
                    ? client.recognizePng(language, image) : client.recognizeJpeg(language, image);
            if (result == null || result.lines() == null) {
                throw new OcrProcessingException();
            }
            return result.lines();
        } catch (RetryableException error) {
            log.warn("OCR recognition transport failed: {}", error.getMessage());
            throw new OcrUnavailableException();
        } catch (DecodeException error) {
            throw new OcrProcessingException();
        } catch (FeignException error) {
            log.warn("OCR recognition call failed: {}", error.getMessage());
            throw new OcrUnavailableException();
        }
    }

    @Override
    public byte[] renderDocx(List<RecognizedPageDto> pages) {
        try {
            ResponseEntity<byte[]> response = client.renderDocx(new DocxRequestDto(pages));
            if (response == null || response.getBody() == null || response.getBody().length == 0
                    || response.getHeaders().getContentType() == null
                    || !response.getHeaders().getContentType().toString().startsWith(DOCX_CONTENT_TYPE)) {
                throw new OcrProcessingException();
            }
            return response.getBody();
        } catch (RetryableException error) {
            log.warn("DOCX render transport failed: {}", error.getMessage());
            throw new OcrUnavailableException();
        } catch (DecodeException error) {
            throw new OcrProcessingException();
        } catch (FeignException error) {
            log.warn("DOCX render call failed: {}", error.getMessage());
            throw new OcrUnavailableException();
        }
    }
}
