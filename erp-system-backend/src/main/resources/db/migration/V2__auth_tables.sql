SET search_path TO erp_system, public;

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(190) NOT NULL UNIQUE,
    phone VARCHAR(30) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_users_role CHECK (role IN ('ADMIN', 'USER'))
);

CREATE TABLE IF NOT EXISTS user_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    full_name VARCHAR(150) NOT NULL,
    profile_image TEXT,
    national_id VARCHAR(60),
    company_name VARCHAR(180),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_user_profiles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_users_role ON users (role);
CREATE INDEX IF NOT EXISTS idx_user_profiles_user_id ON user_profiles (user_id);

INSERT INTO users (id, username, email, phone, password, role, is_active)
VALUES (
    1,
    'admin',
    'admin@erp.local',
    '+96890000000',
    '$2a$10$sQjluJV8UW2VGbVkNhFLQOholmFrXgzBwSQM0psCejRwIwo0TKrcy',
    'ADMIN',
    TRUE
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO user_profiles (id, user_id, full_name, profile_image, national_id, company_name)
VALUES (
    1,
    1,
    'ERP Administrator',
    NULL,
    NULL,
    NULL
)
ON CONFLICT (id) DO NOTHING;

SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));
SELECT setval('user_profiles_id_seq', (SELECT MAX(id) FROM user_profiles));
