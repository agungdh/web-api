CREATE TABLE tag (
    id BIGSERIAL PRIMARY KEY,
    uuid UUID UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL,
    created_by BIGINT,
    updated_at TIMESTAMPTZ,
    updated_by BIGINT,
    deleted_at TIMESTAMPTZ,
    deleted_by BIGINT,
    CONSTRAINT fk_tag_created_by FOREIGN KEY (created_by) REFERENCES "user"(id),
    CONSTRAINT fk_tag_updated_by FOREIGN KEY (updated_by) REFERENCES "user"(id),
    CONSTRAINT fk_tag_deleted_by FOREIGN KEY (deleted_by) REFERENCES "user"(id)
);
