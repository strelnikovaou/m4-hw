CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE IF NOT EXISTS users
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(255) NOT NULL,
    email       VARCHAR(255) UNIQUE,
    age         INT,
    created_at  TIMESTAMPTZ  NOT NULL
);
COMMENT ON TABLE users IS 'Table of users';