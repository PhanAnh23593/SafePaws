-- Apply to existing databases before running this version. No user records are deleted.
BEGIN;
ALTER TABLE users ALTER COLUMN password TYPE varchar(255);
ALTER TABLE users ALTER COLUMN full_name TYPE text;
INSERT INTO roles (id, name)
VALUES ('00000000-0000-4000-8000-000000000001', 'ROLE_USER')
ON CONFLICT (name) DO NOTHING;
COMMIT;
