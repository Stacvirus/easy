CREATE TABLE app_user (
    id UUID PRIMARY KEY,
    email VARCHAR(320) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE subject (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL REFERENCES app_user(id),
    name VARCHAR(120) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT subject_name_not_blank CHECK (length(btrim(name)) > 0),
    CONSTRAINT subject_id_owner_unique UNIQUE (id, owner_id)
);
CREATE UNIQUE INDEX subject_owner_name_unique ON subject (owner_id, lower(name));

CREATE TABLE document (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL REFERENCES app_user(id),
    subject_id UUID,
    title VARCHAR(200) NOT NULL,
    document_date DATE NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT document_title_not_blank CHECK (length(btrim(title)) > 0),
    CONSTRAINT document_subject_same_owner FOREIGN KEY (subject_id, owner_id)
        REFERENCES subject(id, owner_id)
);
CREATE INDEX document_owner_created_idx ON document (owner_id, created_at DESC);
CREATE INDEX document_owner_subject_date_idx ON document (owner_id, subject_id, document_date);
