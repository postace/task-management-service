-- Create the tasks table with single-table inheritance
CREATE TABLE tasks (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    user_id UUID,
    task_type VARCHAR(20) NOT NULL,
    
    -- Bug specific fields
    severity VARCHAR(20),
    steps_to_reproduce TEXT,
    priority VARCHAR(20),
    environment VARCHAR(100),
    
    -- Feature specific fields
    business_value TEXT,
    deadline DATE,
    acceptance_criteria TEXT,
    estimated_effort INTEGER,
    
    CONSTRAINT fk_task_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Add indexes for performance
CREATE INDEX idx_task_user ON tasks(user_id);
CREATE INDEX idx_task_status ON tasks(status);
CREATE INDEX idx_task_name ON tasks(name);
CREATE INDEX idx_task_type ON tasks(task_type);

-- Add comments for documentation
COMMENT ON TABLE tasks IS 'Base table for all task types using single-table inheritance';
COMMENT ON COLUMN tasks.id IS 'Unique identifier for the task';
COMMENT ON COLUMN tasks.name IS 'Name of the task';
COMMENT ON COLUMN tasks.description IS 'Detailed description of the task';
COMMENT ON COLUMN tasks.status IS 'Current status of the task (OPEN, IN_PROGRESS, DONE)';
COMMENT ON COLUMN tasks.created_at IS 'Timestamp when the task was created';
COMMENT ON COLUMN tasks.updated_at IS 'Timestamp when the task was last updated';
COMMENT ON COLUMN tasks.user_id IS 'ID of the user assigned to this task';
COMMENT ON COLUMN tasks.task_type IS 'Type discriminator for the task (BUG, FEATURE)';

-- Bug specific columns
COMMENT ON COLUMN tasks.severity IS 'Severity level of the bug (LOW, MEDIUM, HIGH, CRITICAL)';
COMMENT ON COLUMN tasks.steps_to_reproduce IS 'Steps to reproduce the bug';
COMMENT ON COLUMN tasks.priority IS 'Priority level of the bug (LOW, MEDIUM, HIGH)';
COMMENT ON COLUMN tasks.environment IS 'Environment where the bug was found';

-- Feature specific columns
COMMENT ON COLUMN tasks.business_value IS 'Business value description';
COMMENT ON COLUMN tasks.deadline IS 'Deadline for implementing the feature';
COMMENT ON COLUMN tasks.acceptance_criteria IS 'Acceptance criteria for the feature';
COMMENT ON COLUMN tasks.estimated_effort IS 'Estimated effort in story points';
