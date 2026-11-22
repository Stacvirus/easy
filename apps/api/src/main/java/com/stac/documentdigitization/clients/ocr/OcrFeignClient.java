package com.stac.documentdigitization.clients.ocr;

import com.stac.documentdigitization.configurations.ocr.OcrFeignConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/** Private Python service endpoints; authentication and failures use client-specific Feign configuration. */
@FeignClient(name = "ocr", url = "${app.ocr.url}", configuration = OcrFeignConfiguration.class)
public interface OcrFeignClient {
    @PostMapping(value = "/internal/recognize", consumes = MediaType.IMAGE_JPEG_VALUE)
    RecognizeResponseDto recognizeJpeg(@RequestParam("language") String language, @RequestBody byte[] image);

    @PostMapping(value = "/internal/recognize", consumes = MediaType.IMAGE_PNG_VALUE)
    RecognizeResponseDto recognizePng(@RequestParam("language") String language, @RequestBody byte[] image);

    @PostMapping(value = "/internal/docx", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<byte[]> renderDocx(@RequestBody DocxRequestDto request);
}
