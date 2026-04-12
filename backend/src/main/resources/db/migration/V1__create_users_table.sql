CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    github_id VARCHAR(255) NOT NULL UNIQUE,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255),
    avatar_url VARCHAR(512),
    elo_rating INTEGER NOT NULL DEFAULT 1200,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_elo_rating ON users(elo_rating);
CREATE INDEX idx_users_github_id ON users(github_id);
