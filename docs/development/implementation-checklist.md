# MVP implementation checklist

- [x] Inspect reference backend and document adopted conventions.
- [x] Bootstrap API, web, OCR, Compose and local setup.
- [x] Benchmark PaddleOCR on public English and French handwriting lines; record accuracy and latency. See [the measured baseline](../architecture/ocr-processing.md). One photographed note was tested without a reference transcription.
- [x] Add identity, ownership, subjects and document metadata with migration V1 and PostgreSQL integration tests.
- [ ] Store uploaded images, validate files and persist page order; verify reorder after reload.
- [ ] Add persistent conversion jobs, ordered OCR, DOCX export, status and authorized download.
- [ ] Build the complete UI workflow, including library filters.
- [ ] Test end-to-end success, privacy, failures, restart recovery and duplicate submissions.

Each item is complete only after its working checkpoint has been run and recorded.

Step 3 checkpoint: seven PostgreSQL Testcontainers integration tests passed (`mvn test`), covering registration, login, ownership, subject management, library filtering, validation, dedicated business errors, and English/French response localization. The API also passed a local PostgreSQL smoke test for registration, subject creation, document creation and OpenAPI.
