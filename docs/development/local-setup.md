# Local setup

Prerequisites: Docker with Compose, Java 21 with Maven, Node 24 with npm, and Python 3.10 or newer.

1. Copy `.env.example` to `.env`. Replace `APP_JWT_SECRET` and `OCR_INTERNAL_TOKEN` with separate random values of at least 32 bytes (for example, generate each with `openssl rand -hex 32`). Before starting the API and OCR directly, load `.env` into each shell with `set -a; source .env; set +a`. Compose uses its example defaults for PostgreSQL and MinIO if `.env` is absent.
2. Start dependencies: `docker compose -f infrastructure/compose.yaml up -d --wait`.
3. Start the API from `apps/api`: `mvn spring-boot:run`. Check `http://localhost:8080/actuator/health`.
4. Start OCR from `apps/ocr-service`: run `python3 -m venv .venv && .venv/bin/pip install -r requirements-dev.txt`, then `.venv/bin/uvicorn app.main:app --reload`. If your Python installation lacks `venv`, use `uv venv --clear --python python3 .venv && uv pip install --python .venv/bin/python -r requirements-dev.txt`. Check `http://localhost:8000/health`. The first recognition downloads the English or French PaddleOCR model; internet access is needed for that first request.
5. Start web from `apps/web`: `npm ci` then `npm run dev`. Open `http://localhost:5173`.

PostgreSQL listens on host port 5433; MinIO API on 9000 and console on 9001. Compose builds MinIO from the tagged upstream source because its former public container images are unavailable. The first build downloads Go dependencies and may take several minutes. The default credentials in `.env.example` are local development values only. Change them before any shared deployment. If ports are changed, update the API datasource environment variables accordingly. Stop dependencies with `docker compose -f infrastructure/compose.yaml down`; add `-v` only when intentionally deleting local data.

The API uses `MINIO_ROOT_USER` and `MINIO_ROOT_PASSWORD` from the sourced `.env` file and creates its private `document-images` bucket on the first upload. Set `MINIO_ENDPOINT` if MinIO is not reachable at `http://localhost:9000`. Page uploads are limited to 10 MB each and 50 MB per multipart request.

For a Java 21 container build without host Java 21, run `docker build -t document-api:local apps/api`. `apps/api/Dockerfile`, `apps/ocr-service/Dockerfile`, and `apps/web/Dockerfile` support container builds, while direct process startup remains the normal development path.

For the optional OCR benchmark, follow [the benchmark commands](../architecture/ocr-processing.md). The normal OCR service startup defers model download until the first recognition request.

API integration tests use an isolated PostgreSQL Testcontainer. From `apps/api`, run `mvn test` with Java 21 and Docker available. The tests do not write to the Compose development database.
