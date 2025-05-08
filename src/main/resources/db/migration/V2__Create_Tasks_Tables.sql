-- Create the base tasks table
CREATE TABLE tasks (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL,
    user_id BIGINT,
    task_type VARCHAR(20) NOT NULL,
    CONSTRAINT fk_task_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Create the bugs table for Bug-specific attributes
CREATE TABLE bugs (
    id BIGINT PRIMARY KEY,
    severity VARCHAR(20) NOT NULL,
    steps_to_reproduce TEXT,
    CONSTRAINT fk_bug_task FOREIGN KEY (id) REFERENCES tasks(id)
);

-- Create the features table for Feature-specific attributes
CREATE TABLE features (
    id BIGINT PRIMARY KEY,
    business_value INTEGER NOT NULL,
    deadline DATE NOT NULL,
    CONSTRAINT fk_feature_task FOREIGN KEY (id) REFERENCES tasks(id)
);

-- Add indexes for performance
CREATE INDEX idx_task_user ON tasks(user_id);
CREATE INDEX idx_task_status ON tasks(status);
CREATE INDEX idx_task_name ON tasks(name);

-- Add comments for documentation
COMMENT ON TABLE tasks IS 'Base table for all task types';
COMMENT ON COLUMN tasks.id IS 'Unique identifier for the task';
COMMENT ON COLUMN tasks.name IS 'Name of the task';
COMMENT ON COLUMN tasks.created_at IS 'Timestamp when the task was created';
COMMENT ON COLUMN tasks.status IS 'Current status of the task (OPEN, IN_PROGRESS, DONE)';
COMMENT ON COLUMN tasks.user_id IS 'ID of the user assigned to this task';
COMMENT ON COLUMN tasks.task_type IS 'Type discriminator for the task (BUG, FEATURE)';

COMMENT ON TABLE bugs IS 'Stores bug-specific attributes';
COMMENT ON COLUMN bugs.id IS 'ID of the bug (references tasks.id)';
COMMENT ON COLUMN bugs.severity IS 'Severity level of the bug (LOW, MEDIUM, HIGH, CRITICAL)';
COMMENT ON COLUMN bugs.steps_to_reproduce IS 'Steps to reproduce the bug';

COMMENT ON TABLE features IS 'Stores feature-specific attributes';
COMMENT ON COLUMN features.id IS 'ID of the feature (references tasks.id)';
COMMENT ON COLUMN features.business_value IS 'Business value on a scale of 1-10';
COMMENT ON COLUMN features.deadline IS 'Deadline for implementing the feature';
