-- Safe to rerun on existing or freshly initialized databases.
-- Old access tokens have no sid and require a new login. Old refresh tokens are revoked.
BEGIN;
ALTER TABLE refresh_tokens ADD COLUMN IF NOT EXISTS session_id uuid;
UPDATE refresh_tokens SET session_id = id, revoked = true WHERE session_id IS NULL;
ALTER TABLE refresh_tokens ALTER COLUMN session_id SET NOT NULL;
CREATE INDEX IF NOT EXISTS ix_refresh_tokens_session ON refresh_tokens(user_id, session_id);
COMMIT;
