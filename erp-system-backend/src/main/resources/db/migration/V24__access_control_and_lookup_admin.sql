SET search_path TO erp_system, public;

CREATE TABLE IF NOT EXISTS access_roles (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(60) NOT NULL UNIQUE,
    name_en VARCHAR(150) NOT NULL,
    name_ar VARCHAR(150),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_system BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS user_access_roles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES access_roles (id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT uq_user_access_roles UNIQUE (user_id, role_id)
);

CREATE TABLE IF NOT EXISTS role_menu_permissions (
    id BIGSERIAL PRIMARY KEY,
    role_id BIGINT NOT NULL REFERENCES access_roles (id) ON DELETE CASCADE,
    menu_item_id VARCHAR(64) NOT NULL REFERENCES ui_menu_items (id) ON DELETE CASCADE,
    can_view BOOLEAN NOT NULL DEFAULT FALSE,
    can_create BOOLEAN NOT NULL DEFAULT FALSE,
    can_edit BOOLEAN NOT NULL DEFAULT FALSE,
    can_delete BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT uq_role_menu_permissions UNIQUE (role_id, menu_item_id)
);

CREATE TABLE IF NOT EXISTS lookup_types (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(60) NOT NULL UNIQUE,
    name_en VARCHAR(150) NOT NULL,
    name_ar VARCHAR(150),
    sort_order INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

ALTER TABLE lookup_values
    ADD COLUMN IF NOT EXISTS name_en VARCHAR(150);

ALTER TABLE lookup_values
    ADD COLUMN IF NOT EXISTS name_ar VARCHAR(150);

UPDATE lookup_values
SET name_en = INITCAP(REPLACE(LOWER(code), '_', ' '))
WHERE name_en IS NULL;

INSERT INTO lookup_types (code, name_en, name_ar, sort_order, is_active, created_by, updated_by)
VALUES
    ('account-types', 'Account Types', 'أنواع الحسابات', 1, TRUE, 'flyway', 'flyway'),
    ('accounting-methods', 'Accounting Methods', 'طرق المحاسبة', 2, TRUE, 'flyway', 'flyway'),
    ('bill-statuses', 'Bill Statuses', 'حالات الفواتير', 3, TRUE, 'flyway', 'flyway'),
    ('budget-statuses', 'Budget Statuses', 'حالات الموازنات', 4, TRUE, 'flyway', 'flyway'),
    ('check-statuses', 'Check Statuses', 'حالات الشيكات', 5, TRUE, 'flyway', 'flyway'),
    ('check-types', 'Check Types', 'أنواع الشيكات', 6, TRUE, 'flyway', 'flyway'),
    ('currencies', 'Currencies', 'العملات', 7, TRUE, 'flyway', 'flyway'),
    ('entry-types', 'Journal Entry Types', 'أنواع القيود', 8, TRUE, 'flyway', 'flyway'),
    ('journal-entry-statuses', 'Journal Entry Statuses', 'حالات القيود', 9, TRUE, 'flyway', 'flyway'),
    ('payment-methods', 'Payment Methods', 'طرق الدفع', 10, TRUE, 'flyway', 'flyway'),
    ('reconciliation-line-statuses', 'Reconciliation Line Statuses', 'حالات سطور المطابقة', 11, TRUE, 'flyway', 'flyway'),
    ('reconciliation-statuses', 'Reconciliation Statuses', 'حالات المطابقة البنكية', 12, TRUE, 'flyway', 'flyway'),
    ('report-periods', 'Report Periods', 'فترات التقارير', 13, TRUE, 'flyway', 'flyway'),
    ('statuses', 'Statuses', 'الحالات', 14, TRUE, 'flyway', 'flyway'),
    ('transaction-statuses', 'Transaction Statuses', 'حالات المعاملات', 15, TRUE, 'flyway', 'flyway'),
    ('transaction-types', 'Transaction Types', 'أنواع المعاملات', 16, TRUE, 'flyway', 'flyway'),
    ('transfer-statuses', 'Transfer Statuses', 'حالات التحويل', 17, TRUE, 'flyway', 'flyway'),
    ('voucher-statuses', 'Voucher Statuses', 'حالات السندات', 18, TRUE, 'flyway', 'flyway'),
    ('voucher-types', 'Voucher Types', 'أنواع السندات', 19, TRUE, 'flyway', 'flyway')
ON CONFLICT (code) DO UPDATE
SET name_en = EXCLUDED.name_en,
    name_ar = EXCLUDED.name_ar,
    sort_order = EXCLUDED.sort_order,
    is_active = EXCLUDED.is_active,
    updated_by = 'flyway';

INSERT INTO access_roles (code, name_en, name_ar, is_active, is_system, created_by, updated_by)
VALUES
    ('ADMIN', 'System Administrator', 'مدير النظام', TRUE, TRUE, 'flyway', 'flyway'),
    ('ACCOUNTANT_STANDARD', 'Standard Accountant', 'محاسب قياسي', TRUE, FALSE, 'flyway', 'flyway'),
    ('TREASURY_OPERATOR', 'Treasury Operator', 'مسؤول خزينة', TRUE, FALSE, 'flyway', 'flyway'),
    ('REPORT_VIEWER', 'Report Viewer', 'مستعرض تقارير', TRUE, FALSE, 'flyway', 'flyway')
ON CONFLICT (code) DO UPDATE
SET name_en = EXCLUDED.name_en,
    name_ar = EXCLUDED.name_ar,
    is_active = EXCLUDED.is_active,
    is_system = EXCLUDED.is_system,
    updated_by = 'flyway';

DELETE FROM role_menu_permissions
WHERE role_id IN (
    SELECT id
    FROM access_roles
    WHERE code IN ('ADMIN', 'ACCOUNTANT_STANDARD', 'TREASURY_OPERATOR', 'REPORT_VIEWER')
);

INSERT INTO role_menu_permissions (role_id, menu_item_id, can_view, can_create, can_edit, can_delete, created_by, updated_by)
SELECT role.id,
       menu.id,
       TRUE,
       TRUE,
       TRUE,
       TRUE,
       'flyway',
       'flyway'
FROM access_roles role
JOIN ui_menu_items menu ON menu.item_type = 'item'
WHERE role.code = 'ADMIN';

INSERT INTO role_menu_permissions (role_id, menu_item_id, can_view, can_create, can_edit, can_delete, created_by, updated_by)
SELECT role.id,
       menu.id,
       TRUE,
       CASE WHEN menu.id IN ('accounts', 'journal-entries', 'payment-vouchers', 'receipt-vouchers', 'transfers', 'transactions', 'invoices', 'checks', 'bank-accounts', 'reconciliation') THEN TRUE ELSE FALSE END,
       CASE WHEN menu.id IN ('accounts', 'journal-entries', 'payment-vouchers', 'receipt-vouchers', 'transfers', 'transactions', 'invoices', 'checks', 'bank-accounts', 'reconciliation', 'settings') THEN TRUE ELSE FALSE END,
       CASE WHEN menu.id IN ('accounts', 'journal-entries', 'payment-vouchers', 'receipt-vouchers', 'transfers', 'transactions', 'invoices', 'checks', 'bank-accounts') THEN TRUE ELSE FALSE END,
       'flyway',
       'flyway'
FROM access_roles role
JOIN ui_menu_items menu ON menu.item_type = 'item'
WHERE role.code = 'ACCOUNTANT_STANDARD'
  AND menu.id <> 'accountants';

INSERT INTO role_menu_permissions (role_id, menu_item_id, can_view, can_create, can_edit, can_delete, created_by, updated_by)
SELECT role.id,
       menu.id,
       TRUE,
       CASE WHEN menu.id IN ('payment-vouchers', 'receipt-vouchers', 'transfers', 'checks', 'reconciliation') THEN TRUE ELSE FALSE END,
       CASE WHEN menu.id IN ('payment-vouchers', 'receipt-vouchers', 'transfers', 'checks', 'bank-accounts', 'reconciliation') THEN TRUE ELSE FALSE END,
       CASE WHEN menu.id IN ('payment-vouchers', 'receipt-vouchers', 'transfers', 'checks') THEN TRUE ELSE FALSE END,
       'flyway',
       'flyway'
FROM access_roles role
JOIN ui_menu_items menu ON menu.item_type = 'item'
WHERE role.code = 'TREASURY_OPERATOR'
  AND menu.id IN ('dashboard', 'payment-vouchers', 'receipt-vouchers', 'transfers', 'transactions', 'checks', 'bank-accounts', 'ledger', 'reconciliation');

INSERT INTO role_menu_permissions (role_id, menu_item_id, can_view, can_create, can_edit, can_delete, created_by, updated_by)
SELECT role.id,
       menu.id,
       TRUE,
       FALSE,
       FALSE,
       FALSE,
       'flyway',
       'flyway'
FROM access_roles role
JOIN ui_menu_items menu ON menu.item_type = 'item'
WHERE role.code = 'REPORT_VIEWER'
  AND menu.id IN ('dashboard', 'ledger', 'reports', 'transactions');

INSERT INTO user_access_roles (user_id, role_id, created_by, updated_by)
SELECT u.id, r.id, 'flyway', 'flyway'
FROM users u
JOIN access_roles r ON r.code = 'ADMIN'
WHERE u.username = 'admin'
  AND NOT EXISTS (
      SELECT 1
      FROM user_access_roles ur
      WHERE ur.user_id = u.id
        AND ur.role_id = r.id
  );

INSERT INTO users (username, email, phone, password, role, is_active, created_by, updated_by)
VALUES
    ('chief.accountant', 'chief.accountant@erp.local', '0501000001', '$2b$10$6CWuV2VRnCMQwvRzQE6LQu7SAHTflMSv6IvQbUfJhE4y.GktFGXiW', 'ACCOUNTANT', TRUE, 'flyway', 'flyway'),
    ('treasury.user', 'treasury.user@erp.local', '0501000002', '$2b$10$6CWuV2VRnCMQwvRzQE6LQu7SAHTflMSv6IvQbUfJhE4y.GktFGXiW', 'ACCOUNTANT', TRUE, 'flyway', 'flyway'),
    ('report.viewer', 'report.viewer@erp.local', '0501000003', '$2b$10$6CWuV2VRnCMQwvRzQE6LQu7SAHTflMSv6IvQbUfJhE4y.GktFGXiW', 'ACCOUNTANT', TRUE, 'flyway', 'flyway'),
    ('finance.manager', 'finance.manager@erp.local', '0501000004', '$2b$10$6CWuV2VRnCMQwvRzQE6LQu7SAHTflMSv6IvQbUfJhE4y.GktFGXiW', 'ACCOUNTANT', TRUE, 'flyway', 'flyway')
ON CONFLICT (username) DO NOTHING;

INSERT INTO user_profiles (user_id, full_name, national_id, company_name, created_by, updated_by)
SELECT u.id,
       CASE u.username
           WHEN 'chief.accountant' THEN 'Chief Accountant'
           WHEN 'treasury.user' THEN 'Treasury Operator'
           WHEN 'report.viewer' THEN 'Report Viewer'
           WHEN 'finance.manager' THEN 'Finance Manager'
           ELSE u.username
       END,
       NULL,
       'ERP Demo',
       'flyway',
       'flyway'
FROM users u
WHERE u.username IN ('chief.accountant', 'treasury.user', 'report.viewer', 'finance.manager')
  AND NOT EXISTS (
      SELECT 1
      FROM user_profiles profile
      WHERE profile.user_id = u.id
  );

INSERT INTO user_access_roles (user_id, role_id, created_by, updated_by)
SELECT u.id, r.id, 'flyway', 'flyway'
FROM users u
JOIN access_roles r ON r.code = 'ACCOUNTANT_STANDARD'
WHERE u.username = 'chief.accountant'
  AND NOT EXISTS (
      SELECT 1
      FROM user_access_roles ur
      WHERE ur.user_id = u.id
        AND ur.role_id = r.id
  );

INSERT INTO user_access_roles (user_id, role_id, created_by, updated_by)
SELECT u.id, r.id, 'flyway', 'flyway'
FROM users u
JOIN access_roles r ON r.code = 'TREASURY_OPERATOR'
WHERE u.username = 'treasury.user'
  AND NOT EXISTS (
      SELECT 1
      FROM user_access_roles ur
      WHERE ur.user_id = u.id
        AND ur.role_id = r.id
  );

INSERT INTO user_access_roles (user_id, role_id, created_by, updated_by)
SELECT u.id, r.id, 'flyway', 'flyway'
FROM users u
JOIN access_roles r ON r.code = 'REPORT_VIEWER'
WHERE u.username = 'report.viewer'
  AND NOT EXISTS (
      SELECT 1
      FROM user_access_roles ur
      WHERE ur.user_id = u.id
        AND ur.role_id = r.id
  );

INSERT INTO user_access_roles (user_id, role_id, created_by, updated_by)
SELECT u.id, r.id, 'flyway', 'flyway'
FROM users u
JOIN access_roles r ON r.code IN ('ACCOUNTANT_STANDARD', 'REPORT_VIEWER')
WHERE u.username = 'finance.manager'
  AND NOT EXISTS (
      SELECT 1
      FROM user_access_roles ur
      WHERE ur.user_id = u.id
        AND ur.role_id = r.id
  );

UPDATE ui_menu_items
SET title_key = 'NAV.ACCESS_MANAGEMENT'
WHERE id = 'accountants';
