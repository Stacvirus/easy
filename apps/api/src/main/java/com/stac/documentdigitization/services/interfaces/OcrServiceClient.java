package com.stac.documentdigitization.services.interfaces;

import com.stac.documentdigitization.clients.ocr.RecognizedLineDto;
import com.stac.documentdigitization.clients.ocr.RecognizedPageDto;
import java.util.List;

/** Internal recognition and DOCX generation boundary. */
public interface OcrServiceClient {
    List<RecognizedLineDto> recognize(byte[] image, String contentType, String language);
    byte[] renderDocx(List<RecognizedPageDto> pages);
}
