# MVP implementation checklist

- [x] Inspect reference backend and document adopted conventions.
- [x] Bootstrap API, web, OCR, Compose and local setup.
- [x] Benchmark PaddleOCR on public English and French handwriting lines; record accuracy and latency. See [the measured baseline](../architecture/ocr-processing.md). One photographed note was tested without a reference transcription.
- [x] Add identity, ownership, subjects and document metadata with migration V1 and PostgreSQL integration tests.
- [x] Store uploaded images, validate files and persist page order; verify reorder after reload.
- [ ] Add persistent conversion jobs, ordered OCR, DOCX export, status and authorized download.
- [ ] Build the complete UI workflow, including library filters.
- [ ] Test end-to-end success, privacy, failures, restart recovery and duplicate submissions.

Each item is complete only after its working checkpoint has been run and recorded.

Step 3 checkpoint: seven PostgreSQL Testcontainers integration tests passed (`mvn test`), covering registration, login, ownership, subject management, library filtering, validation, dedicated business errors, and English/French response localization. The API also passed a local PostgreSQL smoke test for registration, subject creation, document creation and OpenAPI.

Step 4 checkpoint: the API suite passed with 12 tests, then all seven page integration tests passed after adding JPEG and cleanup-retry cases. Tests cover multi-image upload, size and page limits, invalid content, authorization, durable order across requests, preview, deletion and failed-upload cleanup. A live PostgreSQL/MinIO smoke test uploaded two PNGs, fetched the original bytes, reordered them, deleted the document, and confirmed its queued deletions and MinIO objects were cleared.
