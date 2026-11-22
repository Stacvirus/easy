CREATE TABLE document_page (
    id UUID PRIMARY KEY,
    document_id UUID NOT NULL REFERENCES document(id) ON DELETE CASCADE,
    position INTEGER NOT NULL CHECK (position > 0),
    object_key VARCHAR(512) NOT NULL UNIQUE,
    content_type VARCHAR(32) NOT NULL,
    size_bytes BIGINT NOT NULL CHECK (size_bytes > 0),
    uploaded_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT document_page_position_unique UNIQUE (document_id, position) DEFERRABLE INITIALLY DEFERRED
);
CREATE INDEX document_page_document_idx ON document_page (document_id);

CREATE TABLE object_deletion (
    id UUID PRIMARY KEY,
    object_key VARCHAR(512) NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX object_deletion_created_idx ON object_deletion (created_at);
