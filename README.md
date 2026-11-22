# Document Digitization

Monorepo for a student document workflow: React web app, Spring Boot API, and Python OCR service. The web app supports registration, subject and document organization, image upload and ordering, conversion status, and Word downloads. See [the implementation checklist](docs/development/implementation-checklist.md) for completed checkpoints and remaining verification.

## Start locally

See [local setup](docs/development/local-setup.md) for prerequisites, commands and health checks.

## Layout

- `apps/web`: React, TypeScript, Vite, Tailwind and shadcn/ui setup.
- `apps/api`: Java 21 Spring Boot API, PostgreSQL and Liquibase.
- `apps/ocr-service`: FastAPI processing entry point.
- `infrastructure/compose.yaml`: PostgreSQL, MinIO and OCR for local development.
- `docs`: architecture, decisions and development instructions.
