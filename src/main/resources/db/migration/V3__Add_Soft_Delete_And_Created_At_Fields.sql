-- Add soft delete and created_at fields to users table
ALTER TABLE users
    ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN deleted_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();

-- Add soft delete fields to tasks table
ALTER TABLE tasks
    ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN deleted_at TIMESTAMP WITH TIME ZONE;

-- Add comments for documentation
COMMENT ON COLUMN users.deleted IS 'Flag indicating if the user has been soft deleted';
COMMENT ON COLUMN users.deleted_at IS 'Timestamp when the user was soft deleted';
COMMENT ON COLUMN users.created_at IS 'Timestamp when the user was created';

COMMENT ON COLUMN tasks.deleted IS 'Flag indicating if the task has been soft deleted';
COMMENT ON COLUMN tasks.deleted_at IS 'Timestamp when the task was soft deleted';
