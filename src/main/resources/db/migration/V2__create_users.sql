CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    telegram_user_id BIGINT UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE user_credentials (
    user_id BIGINT NOT NULL PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
    password_hash VARCHAR(255) NOT NULL
);

-- demo / password (BCrypt), привязан к telegram chatId = 1
INSERT INTO users (id, username, telegram_user_id)
VALUES (1, 'demo', 1);

INSERT INTO user_credentials (user_id, password_hash)
VALUES (1, '$2a$10$JhKSBP.jDKCfjFyOVdJIv.fBIyOI4PPusgMmHkchXYkisO3LbCWdy');

SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));
