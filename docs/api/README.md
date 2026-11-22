# API contract

The Spring API exposes OpenAPI at `/v3/api-docs` and Swagger UI at `/swagger-ui.html`. Frontend types should be generated from this contract rather than maintained separately.

Current routes use `/api/v1`. `POST /auth/register` and `POST /auth/login` accept email and password and return a 12-hour bearer token. All other feature routes require `Authorization: Bearer <token>`.

- `/subjects`: create (`POST`), list (`GET`), get (`GET /{id}`), rename (`PATCH /{id}`), delete (`DELETE /{id}`). Deletion returns 409 while documents reference the subject.
- `/documents`: create (`POST`), list (`GET`), get (`GET /{id}`), delete (`DELETE /{id}`). The create body has `title`, `documentDate` (`YYYY-MM-DD`) and optional `subjectId`. The client supplies today's local date when the user accepts the default. List filters are optional `subjectId` and `date` query parameters.
- `/documents/{id}/pages`: upload one or more images (`POST` multipart field `files`), list in saved order (`GET`), and replace the full order (`PUT /order` with `{"pageIds":[...]}`). Positions start at 1. `GET /{pageId}/content` returns the original image for an authorized preview, and `DELETE /{pageId}` removes a page. Responses contain stable page IDs and metadata but no object keys.
- `/documents/{id}/conversions`: submit a conversion (`POST`, optional body `{"language":"en"}` or `{"language":"fr"}`) and list jobs (`GET`). Submission returns 202 with a job ID and `PENDING` status; submitting again while a job is pending or processing returns that job. A document needs at least one page.
- `/conversions/{jobId}`: poll a job (`GET`). Status progresses through `PENDING`, `PROCESSING`, then `COMPLETED` or `FAILED`. A failed response includes a stable `failureCode` and localized `failureMessage`; submit again to retry.
- `/documents/{id}/exports`: list completed DOCX exports (`GET`). `GET /{exportId}/download` returns an authorized Word file as an attachment. Each successful conversion creates a separate export.

Uploads accept JPEG and PNG only, with a 10 MB limit per image, a 50 MB request limit, and a maximum of 50 pages per document. The API checks the actual image format and decoded dimensions, not just the filename or declared content type. Reordering must include every current page ID exactly once. Deleted images are queued for retryable cleanup in MinIO.

Owned records belonging to another account return 404. Invalid request bodies return 400; duplicate email or subject name returns 409. Error responses contain `timestamp`, `status`, `errorCode`, `message`, `path`, `correlationId` and `fields`. The same safe `X-Correlation-Id` is returned as a response header and included in request logs.

Error messages and validation field messages follow `Accept-Language`: `en` and `fr` are supported, including regional values such as `fr-FR`. Missing or unsupported languages use English. Stable `errorCode` values and JSON field names do not change with the language.
