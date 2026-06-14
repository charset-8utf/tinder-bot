CREATE TABLE user_sessions (
    telegram_user_id BIGINT NOT NULL PRIMARY KEY,
    payload JSONB NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
