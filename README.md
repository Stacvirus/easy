# Document Digitization

Monorepo for a student document workflow: React web app, Spring Boot API, and Python OCR service. The API supports registration, login, user-owned subjects, document metadata, image uploads, private previews and page ordering. Conversion features are scheduled in [the implementation checklist](docs/development/implementation-checklist.md).

## Start locally

See [local setup](docs/development/local-setup.md) for prerequisites, commands and health checks.

## Layout

- `apps/web`: React, TypeScript, Vite, Tailwind and shadcn/ui setup.
- `apps/api`: Java 21 Spring Boot API, PostgreSQL and Liquibase.
- `apps/ocr-service`: FastAPI processing entry point.
- `infrastructure/compose.yaml`: PostgreSQL and MinIO for local development.
- `docs`: architecture, decisions and development instructions.
