# OCR processing design

The Python service is private to the API and will separate image preprocessing, recognition, structured text results, and DOCX export. The initial real-model candidate is PaddleOCR. Before committing to it, benchmark representative English and French handwriting against known transcriptions and record character error rate, word error rate and processing time. A PaddleOCR detection plus TrOCR recognition pipeline is justified only if measured results warrant it.

The API will submit ordered page references or bytes under an authenticated internal contract. The OCR output must retain page identity and text blocks in reading order. Confidence is included only if the engine supplies meaningful values. DOCX generation uses the structured output and inserts page breaks between source pages. Original images remain in MinIO for reference.

The current service contains only `/health`; it does not claim OCR readiness or recognition quality.
