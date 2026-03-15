CREATE TABLE IF NOT EXISTS tasks (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(120) NOT NULL,
    description TEXT,
    priority VARCHAR(16) NOT NULL CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH')),
    status VARCHAR(32) NOT NULL CHECK (status IN ('PLANNED', 'IN_PROGRESS', 'DONE')),
    due_date DATE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO tasks (title, description, priority, status, due_date)
VALUES
    ('Prepare docker-compose', 'Describe services, network and named volume', 'HIGH', 'IN_PROGRESS', CURRENT_DATE + INTERVAL '2 days'),
    ('Test CRUD endpoints', 'Check API requests through Postman or curl', 'MEDIUM', 'PLANNED', CURRENT_DATE + INTERVAL '4 days');
