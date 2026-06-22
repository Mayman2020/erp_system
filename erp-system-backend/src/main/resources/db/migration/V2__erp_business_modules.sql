-- =============================================================================
-- ERP Business Modules: Inventory, Sales, Purchases, HR, CRM, Projects
-- =============================================================================
SET search_path TO erp_system, public;

-- Common transaction status for operational documents
-- DRAFT, PENDING, APPROVED, CANCELLED

-- ---------------------------------------------------------------------------
-- ERP Settings (extends operational configuration)
-- ---------------------------------------------------------------------------
INSERT INTO accounting_settings (setting_key, setting_value, description) VALUES
('COMPANY_NAME', 'Integrated ERP System', 'Company display name'),
('COMPANY_NAME_AR', 'نظام ERP المتكامل', 'Company name in Arabic'),
('DEFAULT_CURRENCY', 'SAR', 'Default currency code'),
('DEFAULT_TAX_PERCENT', '15', 'Default VAT/tax percentage'),
('SALES_INVOICE_PREFIX', 'SINV', 'Sales invoice number prefix'),
('PURCHASE_INVOICE_PREFIX', 'PINV', 'Purchase invoice number prefix'),
('LOW_STOCK_THRESHOLD', '10', 'Default low stock alert threshold'),
('ALLOW_NEGATIVE_STOCK', 'false', 'Allow selling when stock is insufficient'),
('DEFAULT_LANGUAGE', 'ar', 'Default UI language: ar or en')
ON CONFLICT (setting_key) DO NOTHING;

INSERT INTO numbering_sequences (sequence_name, prefix, current_number, padding_length) VALUES
('PRODUCT_CODE', 'PRD', 100, 5),
('CUSTOMER_CODE', 'CUS', 100, 5),
('SUPPLIER_CODE', 'SUP', 100, 5),
('SALES_QUOTATION', 'SQ', 1, 6),
('SALES_ORDER', 'SO', 1, 6),
('SALES_INVOICE', 'SINV', 1, 6),
('SALES_RETURN', 'SR', 1, 6),
('PURCHASE_ORDER', 'PO', 1, 6),
('PURCHASE_INVOICE', 'PINV', 1, 6),
('PURCHASE_RETURN', 'PR', 1, 6),
('STOCK_MOVEMENT', 'SM', 1, 6),
('EMPLOYEE_CODE', 'EMP', 100, 5),
('PROJECT_CODE', 'PRJ', 1, 5),
('CRM_LEAD', 'LD', 1, 6)
ON CONFLICT (sequence_name) DO NOTHING;

-- ---------------------------------------------------------------------------
-- Inventory
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS product_categories (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(30) NOT NULL UNIQUE,
    name_en VARCHAR(150) NOT NULL,
    name_ar VARCHAR(150),
    parent_id BIGINT REFERENCES product_categories (id),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS units_of_measure (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    name_en VARCHAR(80) NOT NULL,
    name_ar VARCHAR(80),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS warehouses (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(30) NOT NULL UNIQUE,
    name_en VARCHAR(150) NOT NULL,
    name_ar VARCHAR(150),
    location VARCHAR(250),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS products (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    barcode VARCHAR(80),
    name_en VARCHAR(200) NOT NULL,
    name_ar VARCHAR(200),
    category_id BIGINT REFERENCES product_categories (id),
    unit_id BIGINT NOT NULL REFERENCES units_of_measure (id),
    cost_price NUMERIC(19, 2) NOT NULL DEFAULT 0,
    sale_price NUMERIC(19, 2) NOT NULL DEFAULT 0,
    reorder_level NUMERIC(19, 4) NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    description VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS stock_levels (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products (id),
    warehouse_id BIGINT NOT NULL REFERENCES warehouses (id),
    quantity NUMERIC(19, 4) NOT NULL DEFAULT 0,
    reserved_quantity NUMERIC(19, 4) NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT uq_stock_levels_product_warehouse UNIQUE (product_id, warehouse_id)
);

CREATE TABLE IF NOT EXISTS stock_movements (
    id BIGSERIAL PRIMARY KEY,
    movement_number VARCHAR(50) NOT NULL UNIQUE,
    movement_date DATE NOT NULL,
    movement_type VARCHAR(20) NOT NULL,
    product_id BIGINT NOT NULL REFERENCES products (id),
    warehouse_id BIGINT NOT NULL REFERENCES warehouses (id),
    target_warehouse_id BIGINT REFERENCES warehouses (id),
    quantity NUMERIC(19, 4) NOT NULL,
    unit_cost NUMERIC(19, 4) NOT NULL DEFAULT 0,
    reference_type VARCHAR(50),
    reference_id BIGINT,
    notes VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'APPROVED',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT chk_stock_movements_type CHECK (movement_type IN ('IN', 'OUT', 'TRANSFER', 'ADJUSTMENT')),
    CONSTRAINT chk_stock_movements_status CHECK (status IN ('DRAFT', 'PENDING', 'APPROVED', 'CANCELLED'))
);

CREATE INDEX IF NOT EXISTS idx_products_code ON products (code);
CREATE INDEX IF NOT EXISTS idx_products_barcode ON products (barcode);
CREATE INDEX IF NOT EXISTS idx_products_category ON products (category_id);
CREATE INDEX IF NOT EXISTS idx_stock_levels_product ON stock_levels (product_id);
CREATE INDEX IF NOT EXISTS idx_stock_movements_date ON stock_movements (movement_date);
CREATE INDEX IF NOT EXISTS idx_stock_movements_product ON stock_movements (product_id);

-- ---------------------------------------------------------------------------
-- Sales
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS customers (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name_en VARCHAR(200) NOT NULL,
    name_ar VARCHAR(200),
    email VARCHAR(190),
    phone VARCHAR(30),
    tax_number VARCHAR(50),
    address VARCHAR(500),
    credit_limit NUMERIC(19, 2) NOT NULL DEFAULT 0,
    receivable_account_id BIGINT REFERENCES accounts (id),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS sales_quotations (
    id BIGSERIAL PRIMARY KEY,
    quotation_number VARCHAR(50) NOT NULL UNIQUE,
    quotation_date DATE NOT NULL,
    valid_until DATE,
    customer_id BIGINT NOT NULL REFERENCES customers (id),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    subtotal NUMERIC(19, 2) NOT NULL DEFAULT 0,
    discount_amount NUMERIC(19, 2) NOT NULL DEFAULT 0,
    tax_amount NUMERIC(19, 2) NOT NULL DEFAULT 0,
    total_amount NUMERIC(19, 2) NOT NULL DEFAULT 0,
    notes VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT chk_sales_quotations_status CHECK (status IN ('DRAFT', 'PENDING', 'APPROVED', 'CANCELLED'))
);

CREATE TABLE IF NOT EXISTS sales_quotation_lines (
    id BIGSERIAL PRIMARY KEY,
    quotation_id BIGINT NOT NULL REFERENCES sales_quotations (id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products (id),
    description VARCHAR(500),
    quantity NUMERIC(19, 4) NOT NULL,
    unit_price NUMERIC(19, 4) NOT NULL,
    discount_percent NUMERIC(5, 2) NOT NULL DEFAULT 0,
    tax_percent NUMERIC(5, 2) NOT NULL DEFAULT 0,
    line_total NUMERIC(19, 2) NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS sales_orders (
    id BIGSERIAL PRIMARY KEY,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    order_date DATE NOT NULL,
    customer_id BIGINT NOT NULL REFERENCES customers (id),
    quotation_id BIGINT REFERENCES sales_quotations (id),
    warehouse_id BIGINT REFERENCES warehouses (id),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    subtotal NUMERIC(19, 2) NOT NULL DEFAULT 0,
    discount_amount NUMERIC(19, 2) NOT NULL DEFAULT 0,
    tax_amount NUMERIC(19, 2) NOT NULL DEFAULT 0,
    total_amount NUMERIC(19, 2) NOT NULL DEFAULT 0,
    notes VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT chk_sales_orders_status CHECK (status IN ('DRAFT', 'PENDING', 'APPROVED', 'CANCELLED'))
);

CREATE TABLE IF NOT EXISTS sales_order_lines (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES sales_orders (id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products (id),
    description VARCHAR(500),
    quantity NUMERIC(19, 4) NOT NULL,
    unit_price NUMERIC(19, 4) NOT NULL,
    discount_percent NUMERIC(5, 2) NOT NULL DEFAULT 0,
    tax_percent NUMERIC(5, 2) NOT NULL DEFAULT 0,
    line_total NUMERIC(19, 2) NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS sales_invoices (
    id BIGSERIAL PRIMARY KEY,
    invoice_number VARCHAR(50) NOT NULL UNIQUE,
    invoice_date DATE NOT NULL,
    due_date DATE NOT NULL,
    customer_id BIGINT NOT NULL REFERENCES customers (id),
    order_id BIGINT REFERENCES sales_orders (id),
    warehouse_id BIGINT REFERENCES warehouses (id),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    subtotal NUMERIC(19, 2) NOT NULL DEFAULT 0,
    discount_amount NUMERIC(19, 2) NOT NULL DEFAULT 0,
    tax_amount NUMERIC(19, 2) NOT NULL DEFAULT 0,
    total_amount NUMERIC(19, 2) NOT NULL DEFAULT 0,
    paid_amount NUMERIC(19, 2) NOT NULL DEFAULT 0,
    remaining_amount NUMERIC(19, 2) NOT NULL DEFAULT 0,
    notes VARCHAR(500),
    journal_entry_id BIGINT REFERENCES journal_entries (id),
    cancellation_journal_entry_id BIGINT REFERENCES journal_entries (id),
    approved_at TIMESTAMPTZ,
    approved_by VARCHAR(100),
    cancelled_at TIMESTAMPTZ,
    cancelled_by VARCHAR(100),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT chk_sales_invoices_status CHECK (status IN ('DRAFT', 'PENDING', 'APPROVED', 'CANCELLED'))
);

CREATE TABLE IF NOT EXISTS sales_invoice_lines (
    id BIGSERIAL PRIMARY KEY,
    invoice_id BIGINT NOT NULL REFERENCES sales_invoices (id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products (id),
    description VARCHAR(500),
    quantity NUMERIC(19, 4) NOT NULL,
    unit_price NUMERIC(19, 4) NOT NULL,
    discount_percent NUMERIC(5, 2) NOT NULL DEFAULT 0,
    tax_percent NUMERIC(5, 2) NOT NULL DEFAULT 0,
    line_total NUMERIC(19, 2) NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS sales_returns (
    id BIGSERIAL PRIMARY KEY,
    return_number VARCHAR(50) NOT NULL UNIQUE,
    return_date DATE NOT NULL,
    customer_id BIGINT NOT NULL REFERENCES customers (id),
    invoice_id BIGINT REFERENCES sales_invoices (id),
    warehouse_id BIGINT REFERENCES warehouses (id),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    subtotal NUMERIC(19, 2) NOT NULL DEFAULT 0,
    tax_amount NUMERIC(19, 2) NOT NULL DEFAULT 0,
    total_amount NUMERIC(19, 2) NOT NULL DEFAULT 0,
    notes VARCHAR(500),
    journal_entry_id BIGINT REFERENCES journal_entries (id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT chk_sales_returns_status CHECK (status IN ('DRAFT', 'PENDING', 'APPROVED', 'CANCELLED'))
);

CREATE TABLE IF NOT EXISTS sales_return_lines (
    id BIGSERIAL PRIMARY KEY,
    return_id BIGINT NOT NULL REFERENCES sales_returns (id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products (id),
    quantity NUMERIC(19, 4) NOT NULL,
    unit_price NUMERIC(19, 4) NOT NULL,
    line_total NUMERIC(19, 2) NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_customers_code ON customers (code);
CREATE INDEX IF NOT EXISTS idx_sales_invoices_number ON sales_invoices (invoice_number);
CREATE INDEX IF NOT EXISTS idx_sales_invoices_customer ON sales_invoices (customer_id);
CREATE INDEX IF NOT EXISTS idx_sales_invoices_date ON sales_invoices (invoice_date);
CREATE INDEX IF NOT EXISTS idx_sales_invoices_status ON sales_invoices (status);

-- ---------------------------------------------------------------------------
-- Purchases
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS suppliers (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name_en VARCHAR(200) NOT NULL,
    name_ar VARCHAR(200),
    email VARCHAR(190),
    phone VARCHAR(30),
    tax_number VARCHAR(50),
    address VARCHAR(500),
    payable_account_id BIGINT REFERENCES accounts (id),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS purchase_orders (
    id BIGSERIAL PRIMARY KEY,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    order_date DATE NOT NULL,
    supplier_id BIGINT NOT NULL REFERENCES suppliers (id),
    warehouse_id BIGINT REFERENCES warehouses (id),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    subtotal NUMERIC(19, 2) NOT NULL DEFAULT 0,
    discount_amount NUMERIC(19, 2) NOT NULL DEFAULT 0,
    tax_amount NUMERIC(19, 2) NOT NULL DEFAULT 0,
    total_amount NUMERIC(19, 2) NOT NULL DEFAULT 0,
    notes VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT chk_purchase_orders_status CHECK (status IN ('DRAFT', 'PENDING', 'APPROVED', 'CANCELLED'))
);

CREATE TABLE IF NOT EXISTS purchase_order_lines (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES purchase_orders (id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products (id),
    description VARCHAR(500),
    quantity NUMERIC(19, 4) NOT NULL,
    unit_price NUMERIC(19, 4) NOT NULL,
    discount_percent NUMERIC(5, 2) NOT NULL DEFAULT 0,
    tax_percent NUMERIC(5, 2) NOT NULL DEFAULT 0,
    line_total NUMERIC(19, 2) NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS purchase_invoices (
    id BIGSERIAL PRIMARY KEY,
    invoice_number VARCHAR(50) NOT NULL UNIQUE,
    invoice_date DATE NOT NULL,
    due_date DATE NOT NULL,
    supplier_id BIGINT NOT NULL REFERENCES suppliers (id),
    order_id BIGINT REFERENCES purchase_orders (id),
    warehouse_id BIGINT REFERENCES warehouses (id),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    subtotal NUMERIC(19, 2) NOT NULL DEFAULT 0,
    discount_amount NUMERIC(19, 2) NOT NULL DEFAULT 0,
    tax_amount NUMERIC(19, 2) NOT NULL DEFAULT 0,
    total_amount NUMERIC(19, 2) NOT NULL DEFAULT 0,
    paid_amount NUMERIC(19, 2) NOT NULL DEFAULT 0,
    remaining_amount NUMERIC(19, 2) NOT NULL DEFAULT 0,
    notes VARCHAR(500),
    journal_entry_id BIGINT REFERENCES journal_entries (id),
    cancellation_journal_entry_id BIGINT REFERENCES journal_entries (id),
    approved_at TIMESTAMPTZ,
    approved_by VARCHAR(100),
    cancelled_at TIMESTAMPTZ,
    cancelled_by VARCHAR(100),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT chk_purchase_invoices_status CHECK (status IN ('DRAFT', 'PENDING', 'APPROVED', 'CANCELLED'))
);

CREATE TABLE IF NOT EXISTS purchase_invoice_lines (
    id BIGSERIAL PRIMARY KEY,
    invoice_id BIGINT NOT NULL REFERENCES purchase_invoices (id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products (id),
    description VARCHAR(500),
    quantity NUMERIC(19, 4) NOT NULL,
    unit_price NUMERIC(19, 4) NOT NULL,
    discount_percent NUMERIC(5, 2) NOT NULL DEFAULT 0,
    tax_percent NUMERIC(5, 2) NOT NULL DEFAULT 0,
    line_total NUMERIC(19, 2) NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS purchase_returns (
    id BIGSERIAL PRIMARY KEY,
    return_number VARCHAR(50) NOT NULL UNIQUE,
    return_date DATE NOT NULL,
    supplier_id BIGINT NOT NULL REFERENCES suppliers (id),
    invoice_id BIGINT REFERENCES purchase_invoices (id),
    warehouse_id BIGINT REFERENCES warehouses (id),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    subtotal NUMERIC(19, 2) NOT NULL DEFAULT 0,
    tax_amount NUMERIC(19, 2) NOT NULL DEFAULT 0,
    total_amount NUMERIC(19, 2) NOT NULL DEFAULT 0,
    notes VARCHAR(500),
    journal_entry_id BIGINT REFERENCES journal_entries (id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT chk_purchase_returns_status CHECK (status IN ('DRAFT', 'PENDING', 'APPROVED', 'CANCELLED'))
);

CREATE TABLE IF NOT EXISTS purchase_return_lines (
    id BIGSERIAL PRIMARY KEY,
    return_id BIGINT NOT NULL REFERENCES purchase_returns (id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products (id),
    quantity NUMERIC(19, 4) NOT NULL,
    unit_price NUMERIC(19, 4) NOT NULL,
    line_total NUMERIC(19, 2) NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS supplier_payments (
    id BIGSERIAL PRIMARY KEY,
    payment_number VARCHAR(50) NOT NULL UNIQUE,
    payment_date DATE NOT NULL,
    supplier_id BIGINT NOT NULL REFERENCES suppliers (id),
    invoice_id BIGINT REFERENCES purchase_invoices (id),
    amount NUMERIC(19, 2) NOT NULL,
    payment_method VARCHAR(30) NOT NULL DEFAULT 'CASH',
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    notes VARCHAR(500),
    journal_entry_id BIGINT REFERENCES journal_entries (id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT chk_supplier_payments_status CHECK (status IN ('DRAFT', 'PENDING', 'APPROVED', 'CANCELLED'))
);

CREATE INDEX IF NOT EXISTS idx_suppliers_code ON suppliers (code);
CREATE INDEX IF NOT EXISTS idx_purchase_invoices_number ON purchase_invoices (invoice_number);
CREATE INDEX IF NOT EXISTS idx_purchase_invoices_supplier ON purchase_invoices (supplier_id);
CREATE INDEX IF NOT EXISTS idx_purchase_invoices_date ON purchase_invoices (invoice_date);

-- ---------------------------------------------------------------------------
-- HR
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS departments (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(30) NOT NULL UNIQUE,
    name_en VARCHAR(150) NOT NULL,
    name_ar VARCHAR(150),
    manager_id BIGINT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS employees (
    id BIGSERIAL PRIMARY KEY,
    employee_code VARCHAR(50) NOT NULL UNIQUE,
    full_name_en VARCHAR(200) NOT NULL,
    full_name_ar VARCHAR(200),
    email VARCHAR(190),
    phone VARCHAR(30),
    department_id BIGINT REFERENCES departments (id),
    job_title VARCHAR(150),
    hire_date DATE,
    basic_salary NUMERIC(19, 2) NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS attendance_records (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employees (id),
    attendance_date DATE NOT NULL,
    check_in TIME,
    check_out TIME,
    status VARCHAR(20) NOT NULL DEFAULT 'PRESENT',
    notes VARCHAR(300),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT uq_attendance_employee_date UNIQUE (employee_id, attendance_date)
);

CREATE TABLE IF NOT EXISTS leave_requests (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employees (id),
    leave_type VARCHAR(30) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    reason VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT chk_leave_requests_status CHECK (status IN ('DRAFT', 'PENDING', 'APPROVED', 'CANCELLED'))
);

CREATE TABLE IF NOT EXISTS payroll_runs (
    id BIGSERIAL PRIMARY KEY,
    payroll_number VARCHAR(50) NOT NULL UNIQUE,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    total_amount NUMERIC(19, 2) NOT NULL DEFAULT 0,
    notes VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT chk_payroll_runs_status CHECK (status IN ('DRAFT', 'PENDING', 'APPROVED', 'CANCELLED'))
);

CREATE TABLE IF NOT EXISTS payroll_lines (
    id BIGSERIAL PRIMARY KEY,
    payroll_id BIGINT NOT NULL REFERENCES payroll_runs (id) ON DELETE CASCADE,
    employee_id BIGINT NOT NULL REFERENCES employees (id),
    basic_salary NUMERIC(19, 2) NOT NULL DEFAULT 0,
    allowances NUMERIC(19, 2) NOT NULL DEFAULT 0,
    deductions NUMERIC(19, 2) NOT NULL DEFAULT 0,
    net_salary NUMERIC(19, 2) NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS employee_documents (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employees (id),
    document_type VARCHAR(50) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500),
    expiry_date DATE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_employees_code ON employees (employee_code);
CREATE INDEX IF NOT EXISTS idx_employees_department ON employees (department_id);

-- ---------------------------------------------------------------------------
-- CRM
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS crm_leads (
    id BIGSERIAL PRIMARY KEY,
    lead_number VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    company VARCHAR(200),
    email VARCHAR(190),
    phone VARCHAR(30),
    source VARCHAR(50),
    status VARCHAR(20) NOT NULL DEFAULT 'NEW',
    customer_id BIGINT REFERENCES customers (id),
    assigned_to VARCHAR(100),
    notes VARCHAR(1000),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS crm_activities (
    id BIGSERIAL PRIMARY KEY,
    activity_type VARCHAR(30) NOT NULL,
    subject VARCHAR(300) NOT NULL,
    customer_id BIGINT REFERENCES customers (id),
    lead_id BIGINT REFERENCES crm_leads (id),
    activity_date TIMESTAMPTZ NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PLANNED',
    notes VARCHAR(1000),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS crm_notes (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT REFERENCES customers (id),
    lead_id BIGINT REFERENCES crm_leads (id),
    note_text VARCHAR(2000) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- ---------------------------------------------------------------------------
-- Projects
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS projects (
    id BIGSERIAL PRIMARY KEY,
    project_code VARCHAR(50) NOT NULL UNIQUE,
    name_en VARCHAR(200) NOT NULL,
    name_ar VARCHAR(200),
    customer_id BIGINT REFERENCES customers (id),
    start_date DATE,
    end_date DATE,
    budget NUMERIC(19, 2) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'PLANNING',
    description VARCHAR(1000),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS project_tasks (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    title VARCHAR(300) NOT NULL,
    description VARCHAR(1000),
    assigned_employee_id BIGINT REFERENCES employees (id),
    due_date DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'TODO',
    priority VARCHAR(10) NOT NULL DEFAULT 'MEDIUM',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS project_expenses (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    expense_date DATE NOT NULL,
    description VARCHAR(500) NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS project_members (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    employee_id BIGINT NOT NULL REFERENCES employees (id),
    role VARCHAR(50),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_project_members UNIQUE (project_id, employee_id)
);

-- ---------------------------------------------------------------------------
-- Activity Logs
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS activity_logs (
    id BIGSERIAL PRIMARY KEY,
    module_name VARCHAR(50) NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT,
    entity_reference VARCHAR(100),
    description VARCHAR(1000),
    actor VARCHAR(100),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_activity_logs_created ON activity_logs (created_at DESC);
CREATE INDEX IF NOT EXISTS idx_activity_logs_module ON activity_logs (module_name);

-- ---------------------------------------------------------------------------
-- Extended Access Roles
-- ---------------------------------------------------------------------------
INSERT INTO access_roles (code, name_en, name_ar, is_active, is_system, created_by, updated_by)
VALUES
('MANAGER', 'Manager', 'مدير', TRUE, TRUE, 'system', 'system'),
('SALES', 'Sales', 'مبيعات', TRUE, TRUE, 'system', 'system'),
('PURCHASE', 'Purchase', 'مشتريات', TRUE, TRUE, 'system', 'system'),
('INVENTORY', 'Inventory', 'مخزون', TRUE, TRUE, 'system', 'system'),
('HR', 'Human Resources', 'موارد بشرية', TRUE, TRUE, 'system', 'system')
ON CONFLICT (code) DO NOTHING;

-- ---------------------------------------------------------------------------
-- UI Menu Items for new modules
-- ---------------------------------------------------------------------------
INSERT INTO ui_menu_items (id, parent_id, sort_order, item_type, title_key, icon, url, is_external, target_blank, roles_csv, item_classes, breadcrumbs_flag)
VALUES
('erp-sales', NULL, 20, 'GROUP', 'MENU.SALES', 'feather icon-shopping-bag', NULL, FALSE, FALSE, 'ADMIN,ACCOUNTANT_STANDARD,MANAGER,SALES', NULL, FALSE),
('erp-sales-customers', 'erp-sales', 1, 'ITEM', 'MENU.CUSTOMERS', 'feather icon-users', '/sales/customers', FALSE, FALSE, 'ADMIN,ACCOUNTANT_STANDARD,MANAGER,SALES', NULL, TRUE),
('erp-sales-quotations', 'erp-sales', 2, 'ITEM', 'MENU.SALES_QUOTATIONS', 'feather icon-file-text', '/sales/quotations', FALSE, FALSE, 'ADMIN,ACCOUNTANT_STANDARD,MANAGER,SALES', NULL, TRUE),
('erp-sales-orders', 'erp-sales', 3, 'ITEM', 'MENU.SALES_ORDERS', 'feather icon-shopping-cart', '/sales/orders', FALSE, FALSE, 'ADMIN,ACCOUNTANT_STANDARD,MANAGER,SALES', NULL, TRUE),
('erp-sales-invoices', 'erp-sales', 4, 'ITEM', 'MENU.SALES_INVOICES', 'feather icon-file-plus', '/sales/invoices', FALSE, FALSE, 'ADMIN,ACCOUNTANT_STANDARD,MANAGER,SALES', NULL, TRUE),
('erp-sales-returns', 'erp-sales', 5, 'ITEM', 'MENU.SALES_RETURNS', 'feather icon-corner-up-left', '/sales/returns', FALSE, FALSE, 'ADMIN,ACCOUNTANT_STANDARD,MANAGER,SALES', NULL, TRUE),
('erp-purchases', NULL, 30, 'GROUP', 'MENU.PURCHASES', 'feather icon-truck', NULL, FALSE, FALSE, 'ADMIN,ACCOUNTANT_STANDARD,MANAGER,PURCHASE', NULL, FALSE),
('erp-purchases-suppliers', 'erp-purchases', 1, 'ITEM', 'MENU.SUPPLIERS', 'feather icon-briefcase', '/purchases/suppliers', FALSE, FALSE, 'ADMIN,ACCOUNTANT_STANDARD,MANAGER,PURCHASE', NULL, TRUE),
('erp-purchases-orders', 'erp-purchases', 2, 'ITEM', 'MENU.PURCHASE_ORDERS', 'feather icon-shopping-cart', '/purchases/orders', FALSE, FALSE, 'ADMIN,ACCOUNTANT_STANDARD,MANAGER,PURCHASE', NULL, TRUE),
('erp-purchases-invoices', 'erp-purchases', 3, 'ITEM', 'MENU.PURCHASE_INVOICES', 'feather icon-file-plus', '/purchases/invoices', FALSE, FALSE, 'ADMIN,ACCOUNTANT_STANDARD,MANAGER,PURCHASE', NULL, TRUE),
('erp-purchases-returns', 'erp-purchases', 4, 'ITEM', 'MENU.PURCHASE_RETURNS', 'feather icon-corner-up-left', '/purchases/returns', FALSE, FALSE, 'ADMIN,ACCOUNTANT_STANDARD,MANAGER,PURCHASE', NULL, TRUE),
('erp-purchases-payments', 'erp-purchases', 5, 'ITEM', 'MENU.SUPPLIER_PAYMENTS', 'feather icon-credit-card', '/purchases/payments', FALSE, FALSE, 'ADMIN,ACCOUNTANT_STANDARD,MANAGER,PURCHASE', NULL, TRUE),
('erp-inventory', NULL, 40, 'GROUP', 'MENU.INVENTORY', 'feather icon-package', NULL, FALSE, FALSE, 'ADMIN,ACCOUNTANT_STANDARD,MANAGER,INVENTORY', NULL, FALSE),
('erp-inventory-products', 'erp-inventory', 1, 'ITEM', 'MENU.PRODUCTS', 'feather icon-box', '/inventory/products', FALSE, FALSE, 'ADMIN,ACCOUNTANT_STANDARD,MANAGER,INVENTORY', NULL, TRUE),
('erp-inventory-categories', 'erp-inventory', 2, 'ITEM', 'MENU.CATEGORIES', 'feather icon-layers', '/inventory/categories', FALSE, FALSE, 'ADMIN,ACCOUNTANT_STANDARD,MANAGER,INVENTORY', NULL, TRUE),
('erp-inventory-warehouses', 'erp-inventory', 3, 'ITEM', 'MENU.WAREHOUSES', 'feather icon-home', '/inventory/warehouses', FALSE, FALSE, 'ADMIN,ACCOUNTANT_STANDARD,MANAGER,INVENTORY', NULL, TRUE),
('erp-inventory-movements', 'erp-inventory', 4, 'ITEM', 'MENU.STOCK_MOVEMENTS', 'feather icon-repeat', '/inventory/movements', FALSE, FALSE, 'ADMIN,ACCOUNTANT_STANDARD,MANAGER,INVENTORY', NULL, TRUE),
('erp-inventory-stock', 'erp-inventory', 5, 'ITEM', 'MENU.STOCK_LEVELS', 'feather icon-bar-chart-2', '/inventory/stock-levels', FALSE, FALSE, 'ADMIN,ACCOUNTANT_STANDARD,MANAGER,INVENTORY', NULL, TRUE),
('erp-hr', NULL, 50, 'GROUP', 'MENU.HR', 'feather icon-users', NULL, FALSE, FALSE, 'ADMIN,MANAGER,HR', NULL, FALSE),
('erp-hr-departments', 'erp-hr', 1, 'ITEM', 'MENU.DEPARTMENTS', 'feather icon-grid', '/hr/departments', FALSE, FALSE, 'ADMIN,MANAGER,HR', NULL, TRUE),
('erp-hr-employees', 'erp-hr', 2, 'ITEM', 'MENU.EMPLOYEES', 'feather icon-user', '/hr/employees', FALSE, FALSE, 'ADMIN,MANAGER,HR', NULL, TRUE),
('erp-hr-attendance', 'erp-hr', 3, 'ITEM', 'MENU.ATTENDANCE', 'feather icon-clock', '/hr/attendance', FALSE, FALSE, 'ADMIN,MANAGER,HR', NULL, TRUE),
('erp-hr-leave', 'erp-hr', 4, 'ITEM', 'MENU.LEAVE_REQUESTS', 'feather icon-calendar', '/hr/leave-requests', FALSE, FALSE, 'ADMIN,MANAGER,HR', NULL, TRUE),
('erp-hr-payroll', 'erp-hr', 5, 'ITEM', 'MENU.PAYROLL', 'feather icon-dollar-sign', '/hr/payroll', FALSE, FALSE, 'ADMIN,MANAGER,HR', NULL, TRUE),
('erp-crm', NULL, 60, 'GROUP', 'MENU.CRM', 'feather icon-target', NULL, FALSE, FALSE, 'ADMIN,MANAGER,SALES', NULL, FALSE),
('erp-crm-leads', 'erp-crm', 1, 'ITEM', 'MENU.LEADS', 'feather icon-user-plus', '/crm/leads', FALSE, FALSE, 'ADMIN,MANAGER,SALES', NULL, TRUE),
('erp-crm-activities', 'erp-crm', 2, 'ITEM', 'MENU.CRM_ACTIVITIES', 'feather icon-activity', '/crm/activities', FALSE, FALSE, 'ADMIN,MANAGER,SALES', NULL, TRUE),
('erp-projects', NULL, 70, 'GROUP', 'MENU.PROJECTS', 'feather icon-briefcase', NULL, FALSE, FALSE, 'ADMIN,MANAGER', NULL, FALSE),
('erp-projects-list', 'erp-projects', 1, 'ITEM', 'MENU.PROJECT_LIST', 'feather icon-folder', '/projects', FALSE, FALSE, 'ADMIN,MANAGER', NULL, TRUE),
('erp-reports-erp', NULL, 80, 'GROUP', 'MENU.ERP_REPORTS', 'feather icon-pie-chart', NULL, FALSE, FALSE, 'ADMIN,ACCOUNTANT_STANDARD,MANAGER,REPORT_VIEWER', NULL, FALSE),
('erp-reports-sales', 'erp-reports-erp', 1, 'ITEM', 'MENU.SALES_REPORT', 'feather icon-trending-up', '/reports/sales', FALSE, FALSE, 'ADMIN,ACCOUNTANT_STANDARD,MANAGER,REPORT_VIEWER', NULL, TRUE),
('erp-reports-purchases', 'erp-reports-erp', 2, 'ITEM', 'MENU.PURCHASE_REPORT', 'feather icon-trending-down', '/reports/purchases', FALSE, FALSE, 'ADMIN,ACCOUNTANT_STANDARD,MANAGER,REPORT_VIEWER', NULL, TRUE),
('erp-reports-inventory', 'erp-reports-erp', 3, 'ITEM', 'MENU.INVENTORY_REPORT', 'feather icon-package', '/reports/inventory', FALSE, FALSE, 'ADMIN,ACCOUNTANT_STANDARD,MANAGER,REPORT_VIEWER', NULL, TRUE),
('erp-reports-profit', 'erp-reports-erp', 4, 'ITEM', 'MENU.PROFIT_REPORT', 'feather icon-dollar-sign', '/reports/profit', FALSE, FALSE, 'ADMIN,ACCOUNTANT_STANDARD,MANAGER,REPORT_VIEWER', NULL, TRUE)
ON CONFLICT (id) DO NOTHING;

-- Grant menu permissions to ADMIN role for new items
INSERT INTO role_menu_permissions (role_id, menu_item_id, can_view, can_create, can_edit, can_delete, created_by, updated_by)
SELECT r.id, m.id, TRUE, TRUE, TRUE, TRUE, 'system', 'system'
FROM access_roles r
CROSS JOIN ui_menu_items m
WHERE r.code = 'ADMIN'
  AND m.id LIKE 'erp-%'
  AND NOT EXISTS (
      SELECT 1 FROM role_menu_permissions rp
      WHERE rp.role_id = r.id AND rp.menu_item_id = m.id
  );

-- ---------------------------------------------------------------------------
-- Demo Seed Data
-- ---------------------------------------------------------------------------
INSERT INTO units_of_measure (code, name_en, name_ar, created_by) VALUES
('PCS', 'Piece', 'قطعة', 'system'),
('BOX', 'Box', 'صندوق', 'system'),
('KG', 'Kilogram', 'كيلوغرام', 'system')
ON CONFLICT (code) DO NOTHING;

INSERT INTO product_categories (code, name_en, name_ar, created_by) VALUES
('GEN', 'General', 'عام', 'system'),
('ELEC', 'Electronics', 'إلكترونيات', 'system'),
('OFF', 'Office Supplies', 'مستلزمات مكتبية', 'system')
ON CONFLICT (code) DO NOTHING;

INSERT INTO warehouses (code, name_en, name_ar, location, created_by) VALUES
('WH-MAIN', 'Main Warehouse', 'المستودع الرئيسي', 'Riyadh', 'system'),
('WH-BR1', 'Branch Warehouse', 'مستودع الفرع', 'Jeddah', 'system')
ON CONFLICT (code) DO NOTHING;

INSERT INTO products (code, barcode, name_en, name_ar, category_id, unit_id, cost_price, sale_price, reorder_level, created_by)
SELECT 'PRD-001', '8901234567890', 'Laptop Pro 15', 'لابتوب برو 15',
       (SELECT id FROM product_categories WHERE code = 'ELEC'),
       (SELECT id FROM units_of_measure WHERE code = 'PCS'),
       3500.00, 4200.00, 5, 'system'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE code = 'PRD-001');

INSERT INTO products (code, barcode, name_en, name_ar, category_id, unit_id, cost_price, sale_price, reorder_level, created_by)
SELECT 'PRD-002', '8901234567891', 'Office Chair', 'كرسي مكتب',
       (SELECT id FROM product_categories WHERE code = 'OFF'),
       (SELECT id FROM units_of_measure WHERE code = 'PCS'),
       450.00, 650.00, 10, 'system'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE code = 'PRD-002');

INSERT INTO products (code, barcode, name_en, name_ar, category_id, unit_id, cost_price, sale_price, reorder_level, created_by)
SELECT 'PRD-003', '8901234567892', 'A4 Paper Ream', 'رزمة ورق A4',
       (SELECT id FROM product_categories WHERE code = 'OFF'),
       (SELECT id FROM units_of_measure WHERE code = 'BOX'),
       18.00, 25.00, 50, 'system'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE code = 'PRD-003');

INSERT INTO stock_levels (product_id, warehouse_id, quantity, created_by)
SELECT p.id, w.id, 25, 'system'
FROM products p, warehouses w
WHERE p.code = 'PRD-001' AND w.code = 'WH-MAIN'
ON CONFLICT (product_id, warehouse_id) DO NOTHING;

INSERT INTO stock_levels (product_id, warehouse_id, quantity, created_by)
SELECT p.id, w.id, 8, 'system'
FROM products p, warehouses w
WHERE p.code = 'PRD-002' AND w.code = 'WH-MAIN'
ON CONFLICT (product_id, warehouse_id) DO NOTHING;

INSERT INTO stock_levels (product_id, warehouse_id, quantity, created_by)
SELECT p.id, w.id, 3, 'system'
FROM products p, warehouses w
WHERE p.code = 'PRD-003' AND w.code = 'WH-MAIN'
ON CONFLICT (product_id, warehouse_id) DO NOTHING;

INSERT INTO customers (code, name_en, name_ar, email, phone, receivable_account_id, created_by)
SELECT 'CUS-001', 'Al Noor Trading', 'شركة النور للتجارة', 'info@alnoor.local', '+966500000001',
       (SELECT id FROM accounts WHERE code = '1120' LIMIT 1), 'system'
WHERE NOT EXISTS (SELECT 1 FROM customers WHERE code = 'CUS-001');

INSERT INTO customers (code, name_en, name_ar, email, phone, receivable_account_id, created_by)
SELECT 'CUS-002', 'Gulf Services Co.', 'شركة الخليج للخدمات', 'contact@gulf.local', '+966500000002',
       (SELECT id FROM accounts WHERE code = '1120' LIMIT 1), 'system'
WHERE NOT EXISTS (SELECT 1 FROM customers WHERE code = 'CUS-002');

INSERT INTO suppliers (code, name_en, name_ar, email, phone, payable_account_id, created_by)
SELECT 'SUP-001', 'Tech Supplies Ltd', 'تقنية التوريد', 'sales@techsup.local', '+966500000101',
       (SELECT id FROM accounts WHERE code = '2000' LIMIT 1), 'system'
WHERE NOT EXISTS (SELECT 1 FROM suppliers WHERE code = 'SUP-001');

INSERT INTO suppliers (code, name_en, name_ar, email, phone, payable_account_id, created_by)
SELECT 'SUP-002', 'Office World', 'عالم المكاتب', 'orders@officeworld.local', '+966500000102',
       (SELECT id FROM accounts WHERE code = '2000' LIMIT 1), 'system'
WHERE NOT EXISTS (SELECT 1 FROM suppliers WHERE code = 'SUP-002');

INSERT INTO departments (code, name_en, name_ar, created_by) VALUES
('HR', 'Human Resources', 'الموارد البشرية', 'system'),
('SALES', 'Sales', 'المبيعات', 'system'),
('FIN', 'Finance', 'المالية', 'system'),
('IT', 'Information Technology', 'تقنية المعلومات', 'system')
ON CONFLICT (code) DO NOTHING;

INSERT INTO employees (employee_code, full_name_en, full_name_ar, email, department_id, job_title, hire_date, basic_salary, created_by)
SELECT 'EMP-001', 'Ahmed Mohamed', 'أحمد محمد', 'ahmed@erp.local',
       (SELECT id FROM departments WHERE code = 'IT'),
       'System Admin', '2022-01-15', 15000.00, 'system'
WHERE NOT EXISTS (SELECT 1 FROM employees WHERE employee_code = 'EMP-001');

INSERT INTO employees (employee_code, full_name_en, full_name_ar, email, department_id, job_title, hire_date, basic_salary, created_by)
SELECT 'EMP-002', 'Sara Ali', 'سارة علي', 'sara@erp.local',
       (SELECT id FROM departments WHERE code = 'SALES'),
       'Sales Manager', '2021-06-01', 12000.00, 'system'
WHERE NOT EXISTS (SELECT 1 FROM employees WHERE employee_code = 'EMP-002');

INSERT INTO crm_leads (lead_number, name, company, email, phone, source, status, created_by)
SELECT 'LD-000001', 'Khalid Hassan', 'Hassan Group', 'khalid@hassan.local', '+966500000201', 'WEBSITE', 'NEW', 'system'
WHERE NOT EXISTS (SELECT 1 FROM crm_leads WHERE lead_number = 'LD-000001');

INSERT INTO projects (project_code, name_en, name_ar, customer_id, start_date, end_date, budget, status, created_by)
SELECT 'PRJ-001', 'ERP Implementation', 'تنفيذ نظام ERP',
       (SELECT id FROM customers WHERE code = 'CUS-001'),
       CURRENT_DATE - INTERVAL '30 days', CURRENT_DATE + INTERVAL '60 days',
       150000.00, 'IN_PROGRESS', 'system'
WHERE NOT EXISTS (SELECT 1 FROM projects WHERE project_code = 'PRJ-001');

INSERT INTO activity_logs (module_name, action_type, entity_type, entity_reference, description, actor)
VALUES
('INVENTORY', 'CREATE', 'PRODUCT', 'PRD-001', 'Demo product Laptop Pro 15 created', 'system'),
('SALES', 'CREATE', 'CUSTOMER', 'CUS-001', 'Demo customer Al Noor Trading created', 'system'),
('HR', 'CREATE', 'EMPLOYEE', 'EMP-001', 'Demo employee Ahmed Mohamed created', 'system');
