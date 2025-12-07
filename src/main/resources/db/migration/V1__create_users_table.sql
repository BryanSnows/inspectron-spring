CREATE TABLE IF NOT EXISTS users (
    user_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(30) NOT NULL UNIQUE,
    enrollment VARCHAR(20) NOT NULL UNIQUE,
    role VARCHAR(20) NOT NULL,
    email VARCHAR(200) UNIQUE,
    disabled BOOLEAN NOT NULL DEFAULT FALSE,
    password VARCHAR(100) NOT NULL,
    first_access BOOLEAN NOT NULL DEFAULT TRUE,
    user_tutorial BOOLEAN NOT NULL DEFAULT TRUE,
    salt VARCHAR(60) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_users_enrollment ON users (enrollment);

