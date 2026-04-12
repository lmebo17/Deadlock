-- Drop github_id column (replaced by user_providers table)
DROP INDEX IF EXISTS idx_users_github_id;
ALTER TABLE users DROP COLUMN github_id;

-- Add new columns
ALTER TABLE users ADD COLUMN display_name VARCHAR(255);
ALTER TABLE users ADD COLUMN token_version INTEGER NOT NULL DEFAULT 0;

-- Make username nullable (null until user picks one on first login)
ALTER TABLE users ALTER COLUMN username DROP NOT NULL;

-- Make email NOT NULL (required for account linking)
ALTER TABLE users ALTER COLUMN email SET NOT NULL;

-- Create user_providers table for multi-provider support
CREATE TABLE user_providers (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    provider VARCHAR(20) NOT NULL,
    provider_id VARCHAR(255) NOT NULL,
    UNIQUE(provider, provider_id)
);

CREATE INDEX idx_user_providers_user_id ON user_providers(user_id);
