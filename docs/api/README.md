# API contract

The Spring API exposes OpenAPI at `/v3/api-docs` and Swagger UI at `/swagger-ui.html`. Frontend types should be generated from this contract rather than maintained separately.

Current routes use `/api/v1`. `POST /auth/register` and `POST /auth/login` accept email and password and return a 12-hour bearer token. All other feature routes require `Authorization: Bearer <token>`.

- `/subjects`: create (`POST`), list (`GET`), get (`GET /{id}`), rename (`PATCH /{id}`), delete (`DELETE /{id}`). Deletion returns 409 while documents reference the subject.
- `/documents`: create (`POST`), list (`GET`), get (`GET /{id}`), delete (`DELETE /{id}`). The create body has `title`, `documentDate` (`YYYY-MM-DD`) and optional `subjectId`. The client supplies today's local date when the user accepts the default. List filters are optional `subjectId` and `date` query parameters.

Owned records belonging to another account return 404. Invalid request bodies return 400; duplicate email or subject name returns 409. Error responses contain `timestamp`, `status`, `errorCode`, `message`, `path`, `correlationId` and `fields`. The same safe `X-Correlation-Id` is returned as a response header and included in request logs.

Error messages and validation field messages follow `Accept-Language`: `en` and `fr` are supported, including regional values such as `fr-FR`. Missing or unsupported languages use English. Stable `errorCode` values and JSON field names do not change with the language.
