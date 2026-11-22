# Backend design

The backend is a modular monolith following the reference's layered package structure. Controllers accept validated request DTOs and return response DTOs. Services enforce ownership and business rules. Repositories persist metadata, and infrastructure adapters handle object storage and calls to OCR. Use direct constructor injection and interfaces only for actual replaceable boundaries.

Planned service responsibilities are identity, subjects, documents/pages, conversion jobs and storage. Every document query must be scoped to its owner. Reordering must contain every current page ID exactly once. Job creation captures that order before work begins. A bounded worker claims persisted jobs, records success or failure, and makes interrupted jobs recoverable after restart. Downloads resolve an export through an owned document; clients never supply arbitrary object keys.

Liquibase owns schema changes; Hibernate validates the schema. The first changelog is empty because no domain entities exist yet. Each feature will add a versioned changeset alongside its entity and tests.
