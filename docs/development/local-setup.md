# Local setup

Prerequisites: Docker with Compose, Java 21 with Maven, and Node 24 with npm. Python 3.10 or newer is needed only for running OCR outside Compose.

1. Copy `.env.example` to `.env`. Replace `APP_JWT_SECRET` and `OCR_INTERNAL_TOKEN` with separate random values of at least 32 bytes (for example, generate each with `openssl rand -hex 32`). Before starting the API directly, load `.env` into its shell with `set -a; source .env; set +a`.
2. Start PostgreSQL, MinIO and OCR: `docker compose --env-file .env -f infrastructure/compose.yaml up -d --build --wait`. Check OCR at `http://localhost:8000/health`. The first OCR image build installs PaddleOCR and can take several minutes.
3. Start the API from `apps/api`: `mvn spring-boot:run`. Check `http://localhost:8080/actuator/health`.
4. Start web from `apps/web`: `npm ci` then `npm run dev`. Open `http://localhost:5173`. Vite proxies `/api` to `http://localhost:8080`; set `API_PROXY_TARGET=http://localhost:8081` in `apps/web/.env.local` if your API uses port 8081, then restart Vite. For a separately hosted API, set `VITE_API_BASE_URL` at build time. The interface offers English and French; its selected language is saved in the browser and sent to the API in `Accept-Language`.

PostgreSQL listens on host port 5433; MinIO API on 9000 and console on 9001. OCR binds to localhost port 8000 by default and uses the same `OCR_INTERNAL_TOKEN` as the API. Its English and French model weights download on first recognition and persist in the `ocr_models` volume. Pass `--env-file .env` because the Compose file is under `infrastructure/` and the environment file is at the repository root. Compose builds MinIO from the tagged upstream source because its former public container images are unavailable. The first build downloads Go dependencies and may take several minutes. The default credentials in `.env.example` are local development values only. Change them before any shared deployment. If ports are changed, update the API datasource or `OCR_SERVICE_URL` environment variables accordingly. Stop dependencies with `docker compose --env-file .env -f infrastructure/compose.yaml down`; add `-v` only when intentionally deleting local data and cached models.

The API uses `MINIO_ROOT_USER` and `MINIO_ROOT_PASSWORD` from the sourced `.env` file and creates its private `document-images` bucket on the first upload. Set `MINIO_ENDPOINT` if MinIO is not reachable at `http://localhost:9000`. Page uploads are limited to 10 MB each and 50 MB per multipart request.

For a Java 21 container build without host Java 21, run `docker build -t document-api:local apps/api`. `apps/api/Dockerfile` and `apps/web/Dockerfile` support container builds, while the OCR container is started by Compose.

To run OCR directly instead of through Compose, stop the Compose OCR service with `docker compose --env-file .env -f infrastructure/compose.yaml stop ocr`, then from `apps/ocr-service` run `python3 -m venv .venv && .venv/bin/pip install -r requirements-dev.txt` and `.venv/bin/uvicorn app.main:app --reload`. The process needs `OCR_INTERNAL_TOKEN` from `.env`. For the optional OCR benchmark, follow [the benchmark commands](../architecture/ocr-processing.md).

API integration tests use an isolated PostgreSQL Testcontainer. From `apps/api`, run `mvn test` with Java 21 and Docker available. The tests do not write to the Compose development database.

Frontend API types are generated from the running API's OpenAPI document. From `apps/web`, run `npm run generate:api`; set `API_OPENAPI_URL` when the API is not at `http://localhost:8080/v3/api-docs`. Commit the resulting `src/lib/api-types.ts` whenever the API contract changes. See [web localization](web-localization.md) for adding interface translations.
