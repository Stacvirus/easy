# MVP implementation checklist

- [x] Inspect reference backend and document adopted conventions.
- [x] Bootstrap API, web, OCR, Compose and local setup.
- [ ] Benchmark PaddleOCR on representative English and French handwriting; record accuracy and latency.
- [ ] Add identity, ownership, subjects and document metadata with migrations and tests.
- [ ] Store uploaded images, validate files and persist page order; verify reorder after reload.
- [ ] Add persistent conversion jobs, ordered OCR, DOCX export, status and authorized download.
- [ ] Build the complete UI workflow, including library filters.
- [ ] Test end-to-end success, privacy, failures, restart recovery and duplicate submissions.

Each item is complete only after its working checkpoint has been run and recorded.
