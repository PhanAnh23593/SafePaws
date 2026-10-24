CREATE TABLE roles (
    id uuid PRIMARY KEY,
    name varchar(32) NOT NULL UNIQUE
);
CREATE TABLE users (
    id uuid PRIMARY KEY,
    email varchar(254) NOT NULL,
    phone varchar(10) NOT NULL,
    password varchar(255) NOT NULL,
    full_name text NOT NULL,
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL,
    CONSTRAINT uk_users_email UNIQUE (email)
);
CREATE TABLE user_roles (
    user_id uuid NOT NULL REFERENCES users(id),
    role_id uuid NOT NULL REFERENCES roles(id),
    PRIMARY KEY (user_id, role_id)
);
CREATE TABLE refresh_tokens (
    id uuid PRIMARY KEY,
    user_id uuid NOT NULL REFERENCES users(id),
    token_hash varchar(64) NOT NULL UNIQUE,
    session_id uuid NOT NULL,
    expires_at timestamptz NOT NULL,
    revoked boolean NOT NULL,
    created_at timestamptz NOT NULL
);
CREATE INDEX ix_refresh_tokens_user ON refresh_tokens(user_id);
CREATE INDEX ix_refresh_tokens_session ON refresh_tokens(user_id, session_id);
CREATE INDEX ix_refresh_tokens_expiry ON refresh_tokens(expires_at);
INSERT INTO roles (id, name)
VALUES ('00000000-0000-4000-8000-000000000001', 'ROLE_USER')
ON CONFLICT (name) DO NOTHING;
