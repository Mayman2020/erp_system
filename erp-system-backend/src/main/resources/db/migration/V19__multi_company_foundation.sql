SET search_path TO erp_system, public;

CREATE TABLE companies (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(40) NOT NULL UNIQUE,
    name_en VARCHAR(190) NOT NULL,
    name_ar VARCHAR(190) NOT NULL,
    entity_type VARCHAR(20) NOT NULL DEFAULT 'COMPANY',
    parent_id BIGINT REFERENCES companies(id),
    tax_id VARCHAR(80),
    registration_no VARCHAR(80),
    currency_code VARCHAR(3) NOT NULL DEFAULT 'AED',
    country_code VARCHAR(2) NOT NULL DEFAULT 'AE',
    email VARCHAR(190),
    phone VARCHAR(40),
    address TEXT,
    logo_url VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT chk_company_entity_type CHECK (entity_type IN ('COMPANY', 'BRANCH')),
    CONSTRAINT chk_company_parent CHECK (
        (entity_type = 'COMPANY' AND parent_id IS NULL)
        OR (entity_type = 'BRANCH' AND parent_id IS NOT NULL)
    )
);

CREATE INDEX idx_companies_parent ON companies(parent_id);

CREATE TABLE user_company_access (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    company_id BIGINT NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, company_id)
);

CREATE UNIQUE INDEX uq_user_default_company
    ON user_company_access(user_id) WHERE is_default = TRUE;

INSERT INTO companies (code, name_en, name_ar, entity_type, currency_code, country_code)
VALUES ('MAIN', 'Main Company', 'الشركة الرئيسية', 'COMPANY', 'AED', 'AE');

INSERT INTO user_company_access (user_id, company_id, is_default)
SELECT u.id, c.id, TRUE
FROM users u
CROSS JOIN companies c
WHERE c.code = 'MAIN';
