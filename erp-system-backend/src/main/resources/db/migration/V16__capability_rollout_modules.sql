-- Capability rollout: POS, inventory extensions, maintenance, partners, recruitment, PMO,
-- digital literacy, license, backups, alerts, trial balance support tables, menus & permissions.
SET search_path TO erp_system, public;

-- ========== Product UOM / barcodes ==========
CREATE TABLE IF NOT EXISTS product_uom_conversions (
    id              BIGSERIAL PRIMARY KEY,
    product_id      BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    unit_id         BIGINT NOT NULL REFERENCES units_of_measure(id),
    factor_to_base  NUMERIC(19,6) NOT NULL DEFAULT 1,
    is_purchase     BOOLEAN NOT NULL DEFAULT FALSE,
    is_sales        BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(120),
    updated_by      VARCHAR(120),
    CONSTRAINT uk_product_uom UNIQUE (product_id, unit_id)
);

CREATE TABLE IF NOT EXISTS product_barcodes (
    id          BIGSERIAL PRIMARY KEY,
    product_id  BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    barcode     VARCHAR(80) NOT NULL,
    is_primary  BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by  VARCHAR(120),
    updated_by  VARCHAR(120),
    CONSTRAINT uk_product_barcode UNIQUE (barcode)
);

ALTER TABLE products ADD COLUMN IF NOT EXISTS qr_payload VARCHAR(512);
ALTER TABLE products ADD COLUMN IF NOT EXISTS manufacturer VARCHAR(200);

-- ========== Stock incidents / replenishment / goods receipt / RFQ ==========
CREATE TABLE IF NOT EXISTS stock_incidents (
    id              BIGSERIAL PRIMARY KEY,
    incident_no     VARCHAR(40) NOT NULL UNIQUE,
    warehouse_id    BIGINT NOT NULL REFERENCES warehouses(id),
    product_id      BIGINT NOT NULL REFERENCES products(id),
    quantity        NUMERIC(19,4) NOT NULL,
    incident_type   VARCHAR(20) NOT NULL,
    reason_code     VARCHAR(40),
    notes           VARCHAR(500),
    unit_cost       NUMERIC(19,4) NOT NULL DEFAULT 0,
    financial_impact NUMERIC(19,2) NOT NULL DEFAULT 0,
    status          VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    approved_by     VARCHAR(120),
    approved_at     TIMESTAMP,
    journal_entry_id BIGINT,
    movement_id     BIGINT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(120),
    updated_by      VARCHAR(120)
);

CREATE TABLE IF NOT EXISTS replenishment_proposals (
    id              BIGSERIAL PRIMARY KEY,
    warehouse_id    BIGINT NOT NULL REFERENCES warehouses(id),
    product_id      BIGINT NOT NULL REFERENCES products(id),
    current_qty     NUMERIC(19,4) NOT NULL DEFAULT 0,
    reorder_level   NUMERIC(19,4) NOT NULL DEFAULT 0,
    proposed_qty    NUMERIC(19,4) NOT NULL DEFAULT 0,
    status          VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    purchase_order_id BIGINT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(120),
    updated_by      VARCHAR(120)
);

CREATE TABLE IF NOT EXISTS purchase_rfqs (
    id              BIGSERIAL PRIMARY KEY,
    rfq_no          VARCHAR(40) NOT NULL UNIQUE,
    title           VARCHAR(200) NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    due_date        DATE,
    notes           VARCHAR(500),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(120),
    updated_by      VARCHAR(120)
);

CREATE TABLE IF NOT EXISTS purchase_rfq_lines (
    id              BIGSERIAL PRIMARY KEY,
    rfq_id          BIGINT NOT NULL REFERENCES purchase_rfqs(id) ON DELETE CASCADE,
    product_id      BIGINT NOT NULL REFERENCES products(id),
    quantity        NUMERIC(19,4) NOT NULL,
    notes           VARCHAR(300)
);

CREATE TABLE IF NOT EXISTS purchase_rfq_quotes (
    id              BIGSERIAL PRIMARY KEY,
    rfq_id          BIGINT NOT NULL REFERENCES purchase_rfqs(id) ON DELETE CASCADE,
    supplier_id     BIGINT NOT NULL REFERENCES suppliers(id),
    unit_price      NUMERIC(19,4) NOT NULL DEFAULT 0,
    lead_days       INT NOT NULL DEFAULT 0,
    notes           VARCHAR(300),
    is_selected     BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS goods_receipts (
    id              BIGSERIAL PRIMARY KEY,
    receipt_no      VARCHAR(40) NOT NULL UNIQUE,
    supplier_id     BIGINT REFERENCES suppliers(id),
    warehouse_id    BIGINT NOT NULL REFERENCES warehouses(id),
    purchase_order_id BIGINT,
    status          VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    received_at     TIMESTAMP,
    notes           VARCHAR(500),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(120),
    updated_by      VARCHAR(120)
);

CREATE TABLE IF NOT EXISTS goods_receipt_lines (
    id              BIGSERIAL PRIMARY KEY,
    receipt_id      BIGINT NOT NULL REFERENCES goods_receipts(id) ON DELETE CASCADE,
    product_id      BIGINT NOT NULL REFERENCES products(id),
    quantity        NUMERIC(19,4) NOT NULL,
    unit_cost       NUMERIC(19,4) NOT NULL DEFAULT 0
);

-- ========== POS ==========
CREATE TABLE IF NOT EXISTS pos_terminals (
    id              BIGSERIAL PRIMARY KEY,
    code            VARCHAR(40) NOT NULL UNIQUE,
    name            VARCHAR(120) NOT NULL,
    warehouse_id    BIGINT NOT NULL REFERENCES warehouses(id),
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(120),
    updated_by      VARCHAR(120)
);

CREATE TABLE IF NOT EXISTS pos_shifts (
    id              BIGSERIAL PRIMARY KEY,
    shift_no        VARCHAR(40) NOT NULL UNIQUE,
    terminal_id     BIGINT NOT NULL REFERENCES pos_terminals(id),
    cashier_user_id BIGINT NOT NULL REFERENCES users(id),
    warehouse_id    BIGINT NOT NULL REFERENCES warehouses(id),
    status          VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    opening_cash    NUMERIC(19,2) NOT NULL DEFAULT 0,
    closing_cash    NUMERIC(19,2),
    expected_cash   NUMERIC(19,2),
    cash_sales      NUMERIC(19,2) NOT NULL DEFAULT 0,
    card_sales      NUMERIC(19,2) NOT NULL DEFAULT 0,
    credit_sales    NUMERIC(19,2) NOT NULL DEFAULT 0,
    discrepancy     NUMERIC(19,2),
    notes           VARCHAR(500),
    opened_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    closed_at       TIMESTAMP,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(120),
    updated_by      VARCHAR(120)
);

CREATE TABLE IF NOT EXISTS pos_sales (
    id              BIGSERIAL PRIMARY KEY,
    sale_no         VARCHAR(40) NOT NULL UNIQUE,
    shift_id        BIGINT NOT NULL REFERENCES pos_shifts(id),
    customer_id     BIGINT REFERENCES customers(id),
    warehouse_id    BIGINT NOT NULL REFERENCES warehouses(id),
    status          VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
    subtotal        NUMERIC(19,2) NOT NULL DEFAULT 0,
    discount_amount NUMERIC(19,2) NOT NULL DEFAULT 0,
    tax_amount      NUMERIC(19,2) NOT NULL DEFAULT 0,
    total_amount    NUMERIC(19,2) NOT NULL DEFAULT 0,
    payment_method  VARCHAR(20) NOT NULL DEFAULT 'CASH',
    paid_cash       NUMERIC(19,2) NOT NULL DEFAULT 0,
    paid_card       NUMERIC(19,2) NOT NULL DEFAULT 0,
    paid_credit     NUMERIC(19,2) NOT NULL DEFAULT 0,
    idempotency_key VARCHAR(80),
    offline_batch_id VARCHAR(80),
    sales_invoice_id BIGINT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(120),
    updated_by      VARCHAR(120),
    CONSTRAINT uk_pos_sale_idempotency UNIQUE (idempotency_key)
);

CREATE TABLE IF NOT EXISTS pos_sale_lines (
    id              BIGSERIAL PRIMARY KEY,
    sale_id         BIGINT NOT NULL REFERENCES pos_sales(id) ON DELETE CASCADE,
    product_id      BIGINT NOT NULL REFERENCES products(id),
    quantity        NUMERIC(19,4) NOT NULL,
    unit_price      NUMERIC(19,4) NOT NULL,
    discount_amount NUMERIC(19,2) NOT NULL DEFAULT 0,
    tax_rate        NUMERIC(9,4) NOT NULL DEFAULT 0,
    line_total      NUMERIC(19,2) NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS pos_offline_batches (
    id              BIGSERIAL PRIMARY KEY,
    batch_key       VARCHAR(80) NOT NULL UNIQUE,
    terminal_id     BIGINT REFERENCES pos_terminals(id),
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    payload_json    TEXT,
    result_json     TEXT,
    received_at     TIMESTAMP NOT NULL DEFAULT NOW(),
    processed_at    TIMESTAMP,
    created_by      VARCHAR(120)
);

-- ========== Maintenance ==========
CREATE TABLE IF NOT EXISTS maintenance_assets (
    id              BIGSERIAL PRIMARY KEY,
    asset_code      VARCHAR(40) NOT NULL UNIQUE,
    name            VARCHAR(200) NOT NULL,
    serial_no       VARCHAR(80),
    customer_id     BIGINT REFERENCES customers(id),
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    notes           VARCHAR(500),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(120),
    updated_by      VARCHAR(120)
);

CREATE TABLE IF NOT EXISTS maintenance_technicians (
    id              BIGSERIAL PRIMARY KEY,
    employee_id     BIGINT REFERENCES employees(id),
    display_name    VARCHAR(120) NOT NULL,
    skills_csv      VARCHAR(500),
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS maintenance_tickets (
    id              BIGSERIAL PRIMARY KEY,
    ticket_no       VARCHAR(40) NOT NULL UNIQUE,
    asset_id        BIGINT REFERENCES maintenance_assets(id),
    customer_id     BIGINT REFERENCES customers(id),
    title           VARCHAR(200) NOT NULL,
    description     TEXT,
    priority        VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    status          VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    ticket_type     VARCHAR(20) NOT NULL DEFAULT 'CORRECTIVE',
    technician_id   BIGINT REFERENCES maintenance_technicians(id),
    sla_hours       INT,
    opened_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    closed_at       TIMESTAMP,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(120),
    updated_by      VARCHAR(120)
);

CREATE TABLE IF NOT EXISTS maintenance_checklists (
    id              BIGSERIAL PRIMARY KEY,
    ticket_id       BIGINT NOT NULL REFERENCES maintenance_tickets(id) ON DELETE CASCADE,
    item_text       VARCHAR(300) NOT NULL,
    is_done         BOOLEAN NOT NULL DEFAULT FALSE,
    sort_order      INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS maintenance_spare_parts (
    id              BIGSERIAL PRIMARY KEY,
    ticket_id       BIGINT NOT NULL REFERENCES maintenance_tickets(id) ON DELETE CASCADE,
    product_id      BIGINT NOT NULL REFERENCES products(id),
    warehouse_id    BIGINT NOT NULL REFERENCES warehouses(id),
    quantity        NUMERIC(19,4) NOT NULL,
    unit_cost       NUMERIC(19,4) NOT NULL DEFAULT 0,
    movement_id     BIGINT
);

-- ========== Partners / equity ==========
CREATE TABLE IF NOT EXISTS partners (
    id              BIGSERIAL PRIMARY KEY,
    code            VARCHAR(40) NOT NULL UNIQUE,
    name            VARCHAR(200) NOT NULL,
    share_percent   NUMERIC(9,4) NOT NULL DEFAULT 0,
    capital_account_id BIGINT REFERENCES accounts(id),
    drawing_account_id BIGINT REFERENCES accounts(id),
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(120),
    updated_by      VARCHAR(120)
);

CREATE TABLE IF NOT EXISTS partner_transactions (
    id              BIGSERIAL PRIMARY KEY,
    partner_id      BIGINT NOT NULL REFERENCES partners(id),
    txn_type        VARCHAR(20) NOT NULL,
    amount          NUMERIC(19,2) NOT NULL,
    txn_date        DATE NOT NULL DEFAULT CURRENT_DATE,
    notes           VARCHAR(500),
    status          VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    journal_entry_id BIGINT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(120),
    updated_by      VARCHAR(120)
);

CREATE TABLE IF NOT EXISTS profit_distributions (
    id              BIGSERIAL PRIMARY KEY,
    distribution_no VARCHAR(40) NOT NULL UNIQUE,
    period_label    VARCHAR(40) NOT NULL,
    total_profit    NUMERIC(19,2) NOT NULL DEFAULT 0,
    status          VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    approved_at     TIMESTAMP,
    journal_entry_id BIGINT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(120),
    updated_by      VARCHAR(120)
);

CREATE TABLE IF NOT EXISTS profit_distribution_lines (
    id              BIGSERIAL PRIMARY KEY,
    distribution_id BIGINT NOT NULL REFERENCES profit_distributions(id) ON DELETE CASCADE,
    partner_id      BIGINT NOT NULL REFERENCES partners(id),
    share_percent   NUMERIC(9,4) NOT NULL,
    amount          NUMERIC(19,2) NOT NULL
);

-- ========== Recruitment ==========
CREATE TABLE IF NOT EXISTS hr_vacancies (
    id              BIGSERIAL PRIMARY KEY,
    title           VARCHAR(200) NOT NULL,
    department_id   BIGINT REFERENCES departments(id),
    status          VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    openings        INT NOT NULL DEFAULT 1,
    description     TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(120),
    updated_by      VARCHAR(120)
);

CREATE TABLE IF NOT EXISTS hr_candidates (
    id              BIGSERIAL PRIMARY KEY,
    full_name       VARCHAR(200) NOT NULL,
    email           VARCHAR(200),
    phone           VARCHAR(40),
    vacancy_id      BIGINT REFERENCES hr_vacancies(id),
    status          VARCHAR(20) NOT NULL DEFAULT 'APPLIED',
    score           NUMERIC(5,2),
    notes           VARCHAR(500),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(120),
    updated_by      VARCHAR(120)
);

CREATE TABLE IF NOT EXISTS hr_interviews (
    id              BIGSERIAL PRIMARY KEY,
    candidate_id    BIGINT NOT NULL REFERENCES hr_candidates(id) ON DELETE CASCADE,
    scheduled_at    TIMESTAMP NOT NULL,
    interviewer     VARCHAR(120),
    result          VARCHAR(40),
    notes           VARCHAR(500),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ========== PMO / digital literacy ==========
CREATE TABLE IF NOT EXISTS pmo_milestones (
    id              BIGSERIAL PRIMARY KEY,
    project_id      BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    title           VARCHAR(200) NOT NULL,
    due_date        DATE,
    status          VARCHAR(20) NOT NULL DEFAULT 'PLANNED',
    sort_order      INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS pmo_risks (
    id              BIGSERIAL PRIMARY KEY,
    project_id      BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    title           VARCHAR(200) NOT NULL,
    severity        VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    status          VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    mitigation      VARCHAR(500)
);

CREATE TABLE IF NOT EXISTS pmo_issues (
    id              BIGSERIAL PRIMARY KEY,
    project_id      BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    title           VARCHAR(200) NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    owner_name      VARCHAR(120),
    notes           VARCHAR(500)
);

CREATE TABLE IF NOT EXISTS digital_courses (
    id              BIGSERIAL PRIMARY KEY,
    code            VARCHAR(40) NOT NULL UNIQUE,
    title           VARCHAR(200) NOT NULL,
    description     TEXT,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS digital_enrollments (
    id              BIGSERIAL PRIMARY KEY,
    course_id       BIGINT NOT NULL REFERENCES digital_courses(id),
    employee_id     BIGINT NOT NULL REFERENCES employees(id),
    progress_pct    NUMERIC(5,2) NOT NULL DEFAULT 0,
    score           NUMERIC(5,2),
    status          VARCHAR(20) NOT NULL DEFAULT 'ENROLLED',
    completed_at    TIMESTAMP,
    certificate_no  VARCHAR(60),
    CONSTRAINT uk_digital_enrollment UNIQUE (course_id, employee_id)
);

-- ========== License / backups / alerts ==========
CREATE TABLE IF NOT EXISTS system_licenses (
    id              BIGSERIAL PRIMARY KEY,
    license_key     VARCHAR(120) NOT NULL UNIQUE,
    customer_name   VARCHAR(200) NOT NULL,
    modules_csv     VARCHAR(500),
    max_users       INT NOT NULL DEFAULT 10,
    valid_from      DATE NOT NULL,
    valid_to        DATE NOT NULL,
    grace_days      INT NOT NULL DEFAULT 7,
    signature       TEXT NOT NULL,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    activated_at    TIMESTAMP,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS backup_jobs (
    id              BIGSERIAL PRIMARY KEY,
    job_no          VARCHAR(40) NOT NULL UNIQUE,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    trigger_type    VARCHAR(20) NOT NULL DEFAULT 'MANUAL',
    file_path       VARCHAR(500),
    file_size_bytes BIGINT,
    checksum_sha256 VARCHAR(64),
    error_message   VARCHAR(1000),
    started_at      TIMESTAMP,
    finished_at     TIMESTAMP,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(120)
);

CREATE TABLE IF NOT EXISTS alert_rules (
    id              BIGSERIAL PRIMARY KEY,
    code            VARCHAR(40) NOT NULL UNIQUE,
    title_ar        VARCHAR(200) NOT NULL,
    title_en        VARCHAR(200) NOT NULL,
    severity        VARCHAR(20) NOT NULL DEFAULT 'WARNING',
    entity_type     VARCHAR(40) NOT NULL,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    cooldown_minutes INT NOT NULL DEFAULT 60,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS alert_events (
    id              BIGSERIAL PRIMARY KEY,
    rule_id         BIGINT REFERENCES alert_rules(id),
    title           VARCHAR(300) NOT NULL,
    body            TEXT,
    severity        VARCHAR(20) NOT NULL DEFAULT 'WARNING',
    entity_type     VARCHAR(40),
    entity_ref      VARCHAR(80),
    deep_link       VARCHAR(300),
    status          VARCHAR(20) NOT NULL DEFAULT 'NEW',
    dedupe_key      VARCHAR(120),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    acknowledged_at TIMESTAMP,
    CONSTRAINT uk_alert_dedupe UNIQUE (dedupe_key)
);

CREATE TABLE IF NOT EXISTS leave_balances (
    id              BIGSERIAL PRIMARY KEY,
    employee_id     BIGINT NOT NULL REFERENCES employees(id),
    leave_type      VARCHAR(40) NOT NULL,
    balance_days    NUMERIC(9,2) NOT NULL DEFAULT 0,
    year            INT NOT NULL,
    CONSTRAINT uk_leave_balance UNIQUE (employee_id, leave_type, year)
);

INSERT INTO numbering_sequences (sequence_name, prefix, current_number, padding_length)
VALUES
('POS_SHIFT', 'PSH', 1, 6),
('POS_SALE', 'POS', 1, 6),
('STOCK_INCIDENT', 'INC', 1, 6),
('MAINT_TICKET', 'MTK', 1, 6),
('PARTNER_DIST', 'PDI', 1, 6),
('GOODS_RECEIPT', 'GRN', 1, 6),
('PURCHASE_RFQ', 'RFQ', 1, 6),
('BACKUP_JOB', 'BKP', 1, 6)
ON CONFLICT (sequence_name) DO NOTHING;

-- Seed default POS terminal if warehouse exists
INSERT INTO pos_terminals (code, name, warehouse_id, created_by, updated_by)
SELECT 'POS-01', 'Main POS Terminal', w.id, 'system', 'system'
FROM warehouses w
WHERE w.code = 'MAIN' OR w.id = (SELECT MIN(id) FROM warehouses)
LIMIT 1
ON CONFLICT (code) DO NOTHING;

INSERT INTO alert_rules (code, title_ar, title_en, severity, entity_type)
VALUES
('OVERDUE_INVOICE', 'فاتورة متأخرة السداد', 'Overdue sales invoice', 'WARNING', 'SALES_INVOICE'),
('LOW_STOCK', 'نقص مخزون', 'Low stock', 'WARNING', 'STOCK'),
('NEG_PARTNER_BALANCE', 'رصيد شريك سالب', 'Negative partner balance', 'CRITICAL', 'PARTNER')
ON CONFLICT (code) DO NOTHING;

-- Menus
INSERT INTO ui_menu_items (id, parent_id, sort_order, item_type, title_key, icon, url, is_external, target_blank, roles_csv, item_classes, breadcrumbs_flag)
VALUES
('erp-pos', NULL, 5, 'group', 'MENU.POS', 'point_of_sale', NULL, FALSE, FALSE, 'ADMIN,MANAGER,CASHIER', NULL, FALSE),
('erp-pos-sale', 'erp-pos', 1, 'item', 'MENU.POS_SALE', 'shopping_cart', '/pos/sale', FALSE, FALSE, 'ADMIN,MANAGER,CASHIER', NULL, TRUE),
('erp-pos-shifts', 'erp-pos', 2, 'item', 'MENU.POS_SHIFTS', 'schedule', '/pos/shifts', FALSE, FALSE, 'ADMIN,MANAGER,CASHIER', NULL, TRUE),
('erp-inventory-incidents', 'erp-inventory', 80, 'item', 'MENU.STOCK_INCIDENTS', 'report_problem', '/inventory/incidents', FALSE, FALSE, 'ADMIN,MANAGER,STOREKEEPER', NULL, TRUE),
('erp-inventory-replenishment', 'erp-inventory', 85, 'item', 'MENU.REPLENISHMENT', 'autorenew', '/inventory/replenishment', FALSE, FALSE, 'ADMIN,MANAGER,STOREKEEPER', NULL, TRUE),
('erp-inventory-labels', 'erp-inventory', 90, 'item', 'MENU.BARCODE_LABELS', 'qr_code_2', '/inventory/labels', FALSE, FALSE, 'ADMIN,MANAGER,STOREKEEPER', NULL, TRUE),
('erp-purchases-rfqs', 'erp-purchases', 70, 'item', 'MENU.RFQ', 'request_quote', '/purchases/rfqs', FALSE, FALSE, 'ADMIN,MANAGER', NULL, TRUE),
('erp-purchases-receipts', 'erp-purchases', 75, 'item', 'MENU.GOODS_RECEIPTS', 'inventory', '/purchases/receipts', FALSE, FALSE, 'ADMIN,MANAGER,STOREKEEPER', NULL, TRUE),
('erp-maintenance', NULL, 45, 'group', 'MENU.MAINTENANCE', 'build_circle', NULL, FALSE, FALSE, 'ADMIN,MANAGER,TECHNICIAN', NULL, FALSE),
('erp-maintenance-tickets', 'erp-maintenance', 1, 'item', 'MENU.MAINTENANCE_TICKETS', 'confirmation_number', '/maintenance/tickets', FALSE, FALSE, 'ADMIN,MANAGER,TECHNICIAN', NULL, TRUE),
('erp-maintenance-assets', 'erp-maintenance', 2, 'item', 'MENU.MAINTENANCE_ASSETS', 'devices', '/maintenance/assets', FALSE, FALSE, 'ADMIN,MANAGER,TECHNICIAN', NULL, TRUE),
('erp-partners', NULL, 58, 'group', 'MENU.PARTNERS', 'handshake', NULL, FALSE, FALSE, 'ADMIN,MANAGER,ACCOUNTANT_STANDARD', NULL, FALSE),
('erp-partners-list', 'erp-partners', 1, 'item', 'MENU.PARTNERS_LIST', 'groups', '/partners', FALSE, FALSE, 'ADMIN,MANAGER,ACCOUNTANT_STANDARD', NULL, TRUE),
('erp-partners-distributions', 'erp-partners', 2, 'item', 'MENU.PROFIT_DISTRIBUTIONS', 'pie_chart', '/partners/distributions', FALSE, FALSE, 'ADMIN,MANAGER,ACCOUNTANT_STANDARD', NULL, TRUE),
('erp-hr-recruitment', 'erp-hr', 90, 'item', 'MENU.RECRUITMENT', 'person_search', '/hr/recruitment', FALSE, FALSE, 'ADMIN,MANAGER,HR', NULL, TRUE),
('erp-pmo', 'erp-projects', 20, 'item', 'MENU.PMO', 'account_tree', '/pmo', FALSE, FALSE, 'ADMIN,MANAGER', NULL, TRUE),
('erp-digital-literacy', NULL, 75, 'item', 'MENU.DIGITAL_LITERACY', 'school', '/digital-literacy', FALSE, FALSE, 'ADMIN,MANAGER,HR', NULL, TRUE),
('erp-alerts', NULL, 3, 'item', 'MENU.ALERT_LOG', 'notifications_active', '/alerts', FALSE, FALSE, 'ADMIN,MANAGER,ACCOUNTANT_STANDARD', NULL, TRUE),
('erp-license', 'settings', 90, 'item', 'MENU.LICENSE', 'verified', '/admin/license', FALSE, FALSE, 'ADMIN', NULL, TRUE),
('erp-backups', 'settings', 95, 'item', 'MENU.BACKUPS', 'backup', '/admin/backups', FALSE, FALSE, 'ADMIN', NULL, TRUE)
ON CONFLICT (id) DO UPDATE SET
    parent_id = EXCLUDED.parent_id,
    sort_order = EXCLUDED.sort_order,
    title_key = EXCLUDED.title_key,
    icon = EXCLUDED.icon,
    url = EXCLUDED.url,
    roles_csv = EXCLUDED.roles_csv;

-- Alias menu id used by security filter already inserted above as erp-pos

INSERT INTO role_menu_permissions (role_id, menu_item_id, can_view, can_create, can_edit, can_delete, created_by, updated_by)
SELECT r.id, m.id, TRUE, TRUE, TRUE, TRUE, 'system', 'system'
FROM access_roles r
CROSS JOIN ui_menu_items m
WHERE r.code IN ('ADMIN', 'MANAGER', 'ACCOUNTANT_STANDARD')
  AND m.id IN (
    'erp-pos','erp-pos-sale','erp-pos-shifts',
    'erp-inventory-incidents','erp-inventory-replenishment','erp-inventory-labels',
    'erp-purchases-rfqs','erp-purchases-receipts',
    'erp-maintenance','erp-maintenance-tickets','erp-maintenance-assets',
    'erp-partners','erp-partners-list','erp-partners-distributions',
    'erp-hr-recruitment','erp-pmo','erp-digital-literacy','erp-alerts',
    'erp-license','erp-backups'
  )
  AND NOT EXISTS (
      SELECT 1 FROM role_menu_permissions rp
      WHERE rp.role_id = r.id AND rp.menu_item_id = m.id
  );
