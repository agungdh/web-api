CREATE TABLE post (
    id BIGSERIAL PRIMARY KEY,
    uuid UUID UNIQUE NOT NULL,
    title VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    content TEXT,
    published_at TIMESTAMPTZ,
    category_id BIGINT,
    created_at TIMESTAMPTZ NOT NULL,
    created_by BIGINT,
    updated_at TIMESTAMPTZ,
    updated_by BIGINT,
    deleted_at TIMESTAMPTZ,
    deleted_by BIGINT,
    CONSTRAINT fk_post_category FOREIGN KEY (category_id) REFERENCES category(id),
    CONSTRAINT fk_post_created_by FOREIGN KEY (created_by) REFERENCES "user"(id),
    CONSTRAINT fk_post_updated_by FOREIGN KEY (updated_by) REFERENCES "user"(id),
    CONSTRAINT fk_post_deleted_by FOREIGN KEY (deleted_by) REFERENCES "user"(id)
);
