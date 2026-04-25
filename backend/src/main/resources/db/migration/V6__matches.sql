CREATE TABLE matches (
    id BIGSERIAL PRIMARY KEY,
    player1_id BIGINT NOT NULL REFERENCES users(id),
    player2_id BIGINT NOT NULL REFERENCES users(id),
    problem_id BIGINT NOT NULL REFERENCES problems(id),
    winner_id BIGINT REFERENCES users(id),
    status VARCHAR(20) NOT NULL DEFAULT 'WAITING',
    duration_seconds INTEGER NOT NULL,
    player1_elo_before INTEGER NOT NULL,
    player2_elo_before INTEGER NOT NULL,
    player1_elo_change INTEGER,
    player2_elo_change INTEGER,
    started_at TIMESTAMP NOT NULL DEFAULT NOW(),
    ended_at TIMESTAMP
);

CREATE INDEX idx_matches_player1 ON matches(player1_id);
CREATE INDEX idx_matches_player2 ON matches(player2_id);
CREATE INDEX idx_matches_status ON matches(status);

ALTER TABLE submissions
    ADD CONSTRAINT fk_submissions_match FOREIGN KEY (match_id) REFERENCES matches(id);
