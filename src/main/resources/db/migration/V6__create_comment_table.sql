CREATE TABLE comment (
    id BIGSERIAL PRIMARY KEY,
    uuid UUID UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    content TEXT NOT NULL,
    post_id BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    created_by BIGINT,
    updated_at TIMESTAMPTZ,
    updated_by BIGINT,
    deleted_at TIMESTAMPTZ,
    deleted_by BIGINT,
    CONSTRAINT fk_comment_post FOREIGN KEY (post_id) REFERENCES post(id),
    CONSTRAINT fk_comment_created_by FOREIGN KEY (created_by) REFERENCES "user"(id),
    CONSTRAINT fk_comment_updated_by FOREIGN KEY (updated_by) REFERENCES "user"(id),
    CONSTRAINT fk_comment_deleted_by FOREIGN KEY (deleted_by) REFERENCES "user"(id)
);
