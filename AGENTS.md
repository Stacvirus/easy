# Project conventions

- Inspect `/home/stac/dev/wassa/staff-folder/staff-folder-backend` before changing backend architecture. It is a reference, not a source to copy or edit.
- The API follows its layered layout under `com.stac.documentdigitization`: `controllers`, `dtos`, `services`, `entities`, `repositories`, `mappers`, `exceptions`, `configurations`, and `properties`. Add only packages needed by a real feature.
- `apps/web` owns presentation, `apps/api` owns users, metadata, ownership, jobs and storage orchestration, and `apps/ocr-service` owns recognition and DOCX generation.
- Document important Java classes, interfaces and non-obvious methods with useful JavaDoc. Use Python docstrings and TypeScript TSDoc for important contracts and behavior; avoid comments that restate code.
- Add focused tests with each business feature. Test ownership, page ordering, conversion state and failure paths. Never claim a check passed unless run.
- Define API DTOs and OpenAPI in Spring. Generate frontend types from that contract when endpoints exist; do not maintain duplicate handwritten schemas.
- Keep private files behind ownership checks. Validate uploads, derive storage keys on the server, protect the internal OCR service, and keep secrets out of source and logs.
- Use PostgreSQL metadata, MinIO objects, and Liquibase migrations. Avoid brokers, gateways, Kubernetes and framework layers without a demonstrated need.
- Document content editing inside this application is out of scope. The generated DOCX remains editable in an external editor.
