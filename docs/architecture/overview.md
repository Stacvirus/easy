# Architecture overview

The MVP is three applications around one user workflow:

```text
React web → Spring API → PostgreSQL (users, subjects, documents, pages, jobs, exports)
                       → MinIO (original images, DOCX exports)
                       → private FastAPI OCR service (recognition and DOCX generation)
```

The API owns authentication, ownership checks, metadata, page order and durable conversion jobs. The OCR service receives a fixed ordered set of pages for one job and returns a generated artifact. Only the API can expose private images and exports to a user. The web app presents the workflow and polls job status. A subject is optional for general conversion and selected for the academic flow.

The minimum domain records are User, Subject, Document, DocumentPage, ConversionJob and DocumentExport. A conversion job stores a snapshot of page IDs and order; later reorders can create another export without changing an existing DOCX. PostgreSQL stores object references, not binary files.

The first increment only establishes service startup. See [the checklist](../development/implementation-checklist.md) for the order of working slices.
