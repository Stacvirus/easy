CREATE TABLE conversion_job (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL REFERENCES app_user(id),
    document_id UUID NOT NULL REFERENCES document(id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED')),
    language VARCHAR(2) NOT NULL CHECK (language IN ('en', 'fr')),
    attempts INTEGER NOT NULL DEFAULT 0,
    failure_code VARCHAR(40),
    created_at TIMESTAMPTZ NOT NULL,
    started_at TIMESTAMPTZ,
    finished_at TIMESTAMPTZ
);
CREATE INDEX conversion_job_pending_idx ON conversion_job (status, created_at);
CREATE INDEX conversion_job_owner_idx ON conversion_job (owner_id, created_at DESC);
CREATE UNIQUE INDEX conversion_job_one_active_per_document ON conversion_job (document_id)
    WHERE status IN ('PENDING', 'PROCESSING');

CREATE TABLE conversion_job_page (
    id UUID PRIMARY KEY,
    job_id UUID NOT NULL REFERENCES conversion_job(id) ON DELETE CASCADE,
    position INTEGER NOT NULL CHECK (position > 0),
    page_id UUID NOT NULL,
    object_key VARCHAR(512) NOT NULL,
    content_type VARCHAR(32) NOT NULL,
    CONSTRAINT conversion_job_page_position_unique UNIQUE (job_id, position)
);

CREATE TABLE document_export (
    id UUID PRIMARY KEY,
    job_id UUID NOT NULL UNIQUE REFERENCES conversion_job(id) ON DELETE CASCADE,
    document_id UUID NOT NULL REFERENCES document(id) ON DELETE CASCADE,
    object_key VARCHAR(512) NOT NULL UNIQUE,
    size_bytes BIGINT NOT NULL CHECK (size_bytes > 0),
    created_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX document_export_document_idx ON document_export (document_id, created_at DESC);
