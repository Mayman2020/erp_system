-- =============================================================================
-- Consolidated Flyway migration: former V1 through V47 (single baseline).
-- =============================================================================

-- ---------- source: V1__erp_system_accounting_baseline.sql ----------
CREATE SCHEMA IF NOT EXISTS erp_system;

SET search_path TO erp_system, public;

CREATE TABLE IF NOT EXISTS accounts (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    parent_id BIGINT NULL,
    account_type VARCHAR(20) NOT NULL,
    level INTEGER NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_accounts_parent FOREIGN KEY (parent_id) REFERENCES accounts (id),
    CONSTRAINT chk_accounts_type CHECK (account_type IN ('ASSET', 'LIABILITY', 'EQUITY', 'INCOME', 'EXPENSE')),
    CONSTRAINT chk_accounts_level CHECK (level >= 1)
);

CREATE TABLE IF NOT EXISTS journal_entries (
    id BIGSERIAL PRIMARY KEY,
    entry_date DATE NOT NULL,
    reference VARCHAR(80) NOT NULL,
    description VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_journal_entries_reference UNIQUE (reference)
);

CREATE TABLE IF NOT EXISTS journal_entry_lines (
    id BIGSERIAL PRIMARY KEY,
    journal_entry_id BIGINT NOT NULL,
    account_id BIGINT NOT NULL,
    debit NUMERIC(19, 2) NOT NULL DEFAULT 0,
    credit NUMERIC(19, 2) NOT NULL DEFAULT 0,
    description VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_journal_entry_lines_entry FOREIGN KEY (journal_entry_id) REFERENCES journal_entries (id) ON DELETE CASCADE,
    CONSTRAINT fk_journal_entry_lines_account FOREIGN KEY (account_id) REFERENCES accounts (id),
    CONSTRAINT chk_journal_entry_lines_amounts CHECK (debit >= 0 AND credit >= 0),
    CONSTRAINT chk_journal_entry_lines_side CHECK ((debit = 0 AND credit > 0) OR (credit = 0 AND debit > 0))
);

CREATE TABLE IF NOT EXISTS payment_vouchers (
    id BIGSERIAL PRIMARY KEY,
    voucher_date DATE NOT NULL,
    reference VARCHAR(80) NOT NULL,
    description VARCHAR(500),
    amount NUMERIC(19, 2) NOT NULL,
    cash_account_id BIGINT NOT NULL,
    expense_account_id BIGINT NOT NULL,
    journal_entry_id BIGINT NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_payment_vouchers_cash FOREIGN KEY (cash_account_id) REFERENCES accounts (id),
    CONSTRAINT fk_payment_vouchers_expense FOREIGN KEY (expense_account_id) REFERENCES accounts (id),
    CONSTRAINT fk_payment_vouchers_entry FOREIGN KEY (journal_entry_id) REFERENCES journal_entries (id),
    CONSTRAINT uq_payment_vouchers_reference UNIQUE (reference),
    CONSTRAINT chk_payment_vouchers_amount CHECK (amount > 0)
);

CREATE TABLE IF NOT EXISTS receipt_vouchers (
    id BIGSERIAL PRIMARY KEY,
    voucher_date DATE NOT NULL,
    reference VARCHAR(80) NOT NULL,
    description VARCHAR(500),
    amount NUMERIC(19, 2) NOT NULL,
    cash_account_id BIGINT NOT NULL,
    revenue_account_id BIGINT NOT NULL,
    journal_entry_id BIGINT NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_receipt_vouchers_cash FOREIGN KEY (cash_account_id) REFERENCES accounts (id),
    CONSTRAINT fk_receipt_vouchers_revenue FOREIGN KEY (revenue_account_id) REFERENCES accounts (id),
    CONSTRAINT fk_receipt_vouchers_entry FOREIGN KEY (journal_entry_id) REFERENCES journal_entries (id),
    CONSTRAINT uq_receipt_vouchers_reference UNIQUE (reference),
    CONSTRAINT chk_receipt_vouchers_amount CHECK (amount > 0)
);

CREATE INDEX IF NOT EXISTS idx_accounts_parent_id ON accounts (parent_id);
CREATE INDEX IF NOT EXISTS idx_accounts_type ON accounts (account_type);
CREATE INDEX IF NOT EXISTS idx_journal_entries_entry_date ON journal_entries (entry_date);
CREATE INDEX IF NOT EXISTS idx_journal_entry_lines_entry_id ON journal_entry_lines (journal_entry_id);
CREATE INDEX IF NOT EXISTS idx_journal_entry_lines_account_id ON journal_entry_lines (account_id);
CREATE INDEX IF NOT EXISTS idx_payment_vouchers_voucher_date ON payment_vouchers (voucher_date);
CREATE INDEX IF NOT EXISTS idx_receipt_vouchers_voucher_date ON receipt_vouchers (voucher_date);

INSERT INTO accounts (id, code, name, parent_id, account_type, level, is_active)
VALUES
    (1, '1000', 'Assets', NULL, 'ASSET', 1, TRUE),
    (2, '1100', 'Cash and Cash Equivalents', 1, 'ASSET', 2, TRUE),
    (3, '1110', 'Cash on Hand', 2, 'ASSET', 3, TRUE),
    (4, '1120', 'Bank Account', 2, 'ASSET', 3, TRUE),
    (5, '2000', 'Liabilities', NULL, 'LIABILITY', 1, TRUE),
    (6, '3000', 'Owner Equity', NULL, 'EQUITY', 1, TRUE),
    (7, '4000', 'Operating Revenue', NULL, 'INCOME', 1, TRUE),
    (8, '4100', 'Service Revenue', 7, 'INCOME', 2, TRUE),
    (9, '5000', 'Operating Expenses', NULL, 'EXPENSE', 1, TRUE),
    (10, '5100', 'Office Expenses', 9, 'EXPENSE', 2, TRUE)
ON CONFLICT (id) DO NOTHING;

SELECT setval('accounts_id_seq', (SELECT MAX(id) FROM accounts));


-- ---------- source: V2__auth_tables.sql ----------
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


-- ---------- source: V3__account_names_localized.sql ----------
SET search_path TO erp_system, public;

ALTER TABLE accounts
    ADD COLUMN IF NOT EXISTS name_ar VARCHAR(150),
    ADD COLUMN IF NOT EXISTS name_en VARCHAR(150);

UPDATE accounts
SET
    name_en = COALESCE(NULLIF(name_en, ''), name),
    name_ar = COALESCE(
        NULLIF(name_ar, ''),
        CASE code
            WHEN '1000' THEN 'Ø§Ù„Ø£ØµÙˆÙ„'
            WHEN '1100' THEN 'Ø§Ù„Ù†Ù‚Ø¯ÙŠØ© ÙˆÙ…Ø§ ÙÙŠ Ø­ÙƒÙ…Ù‡Ø§'
            WHEN '1110' THEN 'Ø§Ù„Ù†Ù‚Ø¯ÙŠØ© Ø¨Ø§Ù„ØµÙ†Ø¯ÙˆÙ‚'
            WHEN '1120' THEN 'Ø§Ù„Ø­Ø³Ø§Ø¨ Ø§Ù„Ø¨Ù†ÙƒÙŠ'
            WHEN '2000' THEN 'Ø§Ù„Ø§Ù„ØªØ²Ø§Ù…Ø§Øª'
            WHEN '3000' THEN 'Ø­Ù‚ÙˆÙ‚ Ø§Ù„Ù…Ù„ÙƒÙŠØ©'
            WHEN '4000' THEN 'Ø§Ù„Ø¥ÙŠØ±Ø§Ø¯Ø§Øª Ø§Ù„ØªØ´ØºÙŠÙ„ÙŠØ©'
            WHEN '4100' THEN 'Ø¥ÙŠØ±Ø§Ø¯Ø§Øª Ø§Ù„Ø®Ø¯Ù…Ø§Øª'
            WHEN '5000' THEN 'Ø§Ù„Ù…ØµØ±ÙˆÙØ§Øª Ø§Ù„ØªØ´ØºÙŠÙ„ÙŠØ©'
            WHEN '5100' THEN 'Ù…ØµØ±ÙˆÙØ§Øª Ø§Ù„Ù…ÙƒØªØ¨'
            ELSE name
        END
    );

ALTER TABLE accounts
    ALTER COLUMN name_ar SET NOT NULL,
    ALTER COLUMN name_en SET NOT NULL;


-- ---------- source: V4__full_accounting_schema.sql ----------
-- V4__full_accounting_schema.sql
-- Extend existing tables and add new ones for full accounting module

SET search_path TO erp_system, public;

-- Add missing fields to accounts
ALTER TABLE accounts ADD COLUMN IF NOT EXISTS name_ar VARCHAR(150);
ALTER TABLE accounts ADD COLUMN IF NOT EXISTS full_path VARCHAR(500);
ALTER TABLE accounts ADD COLUMN IF NOT EXISTS is_postable BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE accounts ADD COLUMN IF NOT EXISTS opening_balance NUMERIC(19, 2) NOT NULL DEFAULT 0;
ALTER TABLE accounts ADD COLUMN IF NOT EXISTS opening_balance_side VARCHAR(10) CHECK (opening_balance_side IN ('DEBIT', 'CREDIT'));
ALTER TABLE accounts ADD COLUMN IF NOT EXISTS created_by VARCHAR(100);
ALTER TABLE accounts ADD COLUMN IF NOT EXISTS updated_by VARCHAR(100);

-- Add missing fields to journal_entries
ALTER TABLE journal_entries ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'POSTED', 'REVERSED', 'CANCELLED'));
ALTER TABLE journal_entries ADD COLUMN IF NOT EXISTS posted_at TIMESTAMPTZ;
ALTER TABLE journal_entries ADD COLUMN IF NOT EXISTS reversed_entry_id BIGINT;
ALTER TABLE journal_entries ADD COLUMN IF NOT EXISTS created_by VARCHAR(100);
ALTER TABLE journal_entries ADD COLUMN IF NOT EXISTS updated_by VARCHAR(100);
ALTER TABLE journal_entries ADD COLUMN IF NOT EXISTS posted_by VARCHAR(100);
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_journal_entries_reversed'
          AND conrelid = 'erp_system.journal_entries'::regclass
    ) THEN
        ALTER TABLE journal_entries
            ADD CONSTRAINT fk_journal_entries_reversed
                FOREIGN KEY (reversed_entry_id) REFERENCES journal_entries (id);
    END IF;
END $$;

-- Add missing fields to journal_entry_lines
ALTER TABLE journal_entry_lines ADD COLUMN IF NOT EXISTS created_by VARCHAR(100);
ALTER TABLE journal_entry_lines ADD COLUMN IF NOT EXISTS updated_by VARCHAR(100);

-- Add missing fields to payment_vouchers
ALTER TABLE payment_vouchers ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'APPROVED', 'POSTED', 'CANCELLED'));
ALTER TABLE payment_vouchers ADD COLUMN IF NOT EXISTS payment_method VARCHAR(20) NOT NULL DEFAULT 'CASH' CHECK (payment_method IN ('CASH', 'BANK', 'CHECK'));
ALTER TABLE payment_vouchers ADD COLUMN IF NOT EXISTS account_id BIGINT NOT NULL;
ALTER TABLE payment_vouchers ADD COLUMN IF NOT EXISTS bill_id BIGINT;
ALTER TABLE payment_vouchers ADD COLUMN IF NOT EXISTS posted_at TIMESTAMPTZ;
ALTER TABLE payment_vouchers ADD COLUMN IF NOT EXISTS created_by VARCHAR(100);
ALTER TABLE payment_vouchers ADD COLUMN IF NOT EXISTS updated_by VARCHAR(100);
ALTER TABLE payment_vouchers ADD COLUMN IF NOT EXISTS posted_by VARCHAR(100);
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_payment_vouchers_account'
          AND conrelid = 'erp_system.payment_vouchers'::regclass
    ) THEN
        ALTER TABLE payment_vouchers
            ADD CONSTRAINT fk_payment_vouchers_account
                FOREIGN KEY (account_id) REFERENCES accounts (id);
    END IF;
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_payment_vouchers_amount'
          AND conrelid = 'erp_system.payment_vouchers'::regclass
    ) THEN
        ALTER TABLE payment_vouchers
            ADD CONSTRAINT chk_payment_vouchers_amount CHECK (amount > 0);
    END IF;
END $$;

-- Add missing fields to receipt_vouchers
ALTER TABLE receipt_vouchers ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'APPROVED', 'POSTED', 'CANCELLED'));
ALTER TABLE receipt_vouchers ADD COLUMN IF NOT EXISTS payment_method VARCHAR(20) NOT NULL DEFAULT 'CASH' CHECK (payment_method IN ('CASH', 'BANK', 'CHECK'));
ALTER TABLE receipt_vouchers ADD COLUMN IF NOT EXISTS account_id BIGINT NOT NULL;
ALTER TABLE receipt_vouchers ADD COLUMN IF NOT EXISTS invoice_id BIGINT;
ALTER TABLE receipt_vouchers ADD COLUMN IF NOT EXISTS posted_at TIMESTAMPTZ;
ALTER TABLE receipt_vouchers ADD COLUMN IF NOT EXISTS created_by VARCHAR(100);
ALTER TABLE receipt_vouchers ADD COLUMN IF NOT EXISTS updated_by VARCHAR(100);
ALTER TABLE receipt_vouchers ADD COLUMN IF NOT EXISTS posted_by VARCHAR(100);
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_receipt_vouchers_account'
          AND conrelid = 'erp_system.receipt_vouchers'::regclass
    ) THEN
        ALTER TABLE receipt_vouchers
            ADD CONSTRAINT fk_receipt_vouchers_account
                FOREIGN KEY (account_id) REFERENCES accounts (id);
    END IF;
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_receipt_vouchers_amount'
          AND conrelid = 'erp_system.receipt_vouchers'::regclass
    ) THEN
        ALTER TABLE receipt_vouchers
            ADD CONSTRAINT chk_receipt_vouchers_amount CHECK (amount > 0);
    END IF;
END $$;

-- New tables

CREATE TABLE IF NOT EXISTS transfers (
    id BIGSERIAL PRIMARY KEY,
    transfer_date DATE NOT NULL,
    reference VARCHAR(80) NOT NULL UNIQUE,
    description VARCHAR(500),
    amount NUMERIC(19, 2) NOT NULL,
    source_account_id BIGINT NOT NULL,
    destination_account_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'POSTED', 'CANCELLED')),
    posted_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    posted_by VARCHAR(100),
    CONSTRAINT fk_transfers_source FOREIGN KEY (source_account_id) REFERENCES accounts (id),
    CONSTRAINT fk_transfers_destination FOREIGN KEY (destination_account_id) REFERENCES accounts (id),
    CONSTRAINT chk_transfers_accounts CHECK (source_account_id != destination_account_id),
    CONSTRAINT chk_transfers_amount CHECK (amount > 0)
);

CREATE TABLE IF NOT EXISTS transactions (
    id BIGSERIAL PRIMARY KEY,
    transaction_date DATE NOT NULL,
    reference VARCHAR(80) NOT NULL UNIQUE,
    description VARCHAR(500),
    transaction_type VARCHAR(20) NOT NULL CHECK (transaction_type IN ('SALE', 'PURCHASE', 'REFUND', 'ADJUSTMENT')),
    status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED' CHECK (status IN ('PENDING', 'COMPLETED', 'CANCELLED')),
    amount NUMERIC(19, 2) NOT NULL,
    customer_id BIGINT,
    supplier_id BIGINT,
    related_document_id BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT chk_transactions_amount CHECK (amount > 0)
);

CREATE TABLE IF NOT EXISTS bills (
    id BIGSERIAL PRIMARY KEY,
    bill_number VARCHAR(50) NOT NULL UNIQUE,
    bill_date DATE NOT NULL,
    due_date DATE NOT NULL,
    supplier_id BIGINT NOT NULL,
    description VARCHAR(500),
    subtotal NUMERIC(19, 2) NOT NULL DEFAULT 0,
    tax_amount NUMERIC(19, 2) NOT NULL DEFAULT 0,
    total_amount NUMERIC(19, 2) NOT NULL,
    paid_amount NUMERIC(19, 2) NOT NULL DEFAULT 0,
    outstanding_amount NUMERIC(19, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'APPROVED', 'POSTED', 'PARTIALLY_PAID', 'PAID', 'CANCELLED')),
    posted_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    posted_by VARCHAR(100),
    CONSTRAINT chk_bills_amounts CHECK (total_amount >= 0 AND paid_amount >= 0 AND outstanding_amount >= 0),
    CONSTRAINT chk_bills_dates CHECK (due_date >= bill_date)
);

CREATE TABLE IF NOT EXISTS bill_lines (
    id BIGSERIAL PRIMARY KEY,
    bill_id BIGINT NOT NULL,
    account_id BIGINT NOT NULL,
    description VARCHAR(500),
    quantity NUMERIC(10, 2) NOT NULL DEFAULT 1,
    unit_price NUMERIC(19, 2) NOT NULL,
    line_total NUMERIC(19, 2) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_bill_lines_bill FOREIGN KEY (bill_id) REFERENCES bills (id) ON DELETE CASCADE,
    CONSTRAINT fk_bill_lines_account FOREIGN KEY (account_id) REFERENCES accounts (id),
    CONSTRAINT chk_bill_lines_amounts CHECK (quantity > 0 AND unit_price >= 0 AND line_total >= 0)
);

CREATE TABLE IF NOT EXISTS checks (
    id BIGSERIAL PRIMARY KEY,
    check_number VARCHAR(50) NOT NULL UNIQUE,
    check_type VARCHAR(10) NOT NULL CHECK (check_type IN ('ISSUED', 'RECEIVED')),
    bank_name VARCHAR(100) NOT NULL,
    issue_date DATE NOT NULL,
    due_date DATE NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'DEPOSITED', 'CLEARED', 'BOUNCED', 'CANCELLED')),
    party_name VARCHAR(150),
    linked_payment_id BIGINT,
    linked_receipt_id BIGINT,
    linked_invoice_id BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT chk_checks_amount CHECK (amount > 0),
    CONSTRAINT chk_checks_dates CHECK (due_date >= issue_date)
);

CREATE TABLE IF NOT EXISTS bank_accounts (
    id BIGSERIAL PRIMARY KEY,
    bank_name VARCHAR(100) NOT NULL,
    account_number VARCHAR(50) NOT NULL UNIQUE,
    iban VARCHAR(34),
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    opening_balance NUMERIC(19, 2) NOT NULL DEFAULT 0,
    current_balance NUMERIC(19, 2) NOT NULL DEFAULT 0,
    linked_account_id BIGINT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT fk_bank_accounts_linked FOREIGN KEY (linked_account_id) REFERENCES accounts (id),
    CONSTRAINT uq_bank_accounts_account UNIQUE (linked_account_id)
);

CREATE TABLE IF NOT EXISTS reconciliations (
    id BIGSERIAL PRIMARY KEY,
    bank_account_id BIGINT NOT NULL,
    statement_start_date DATE NOT NULL,
    statement_end_date DATE NOT NULL,
    opening_balance NUMERIC(19, 2) NOT NULL,
    closing_balance NUMERIC(19, 2) NOT NULL,
    system_ending_balance NUMERIC(19, 2) NOT NULL,
    difference NUMERIC(19, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN' CHECK (status IN ('OPEN', 'FINALIZED', 'CANCELLED')),
    finalized_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    finalized_by VARCHAR(100),
    CONSTRAINT fk_reconciliations_bank FOREIGN KEY (bank_account_id) REFERENCES bank_accounts (id),
    CONSTRAINT chk_reconciliations_dates CHECK (statement_end_date >= statement_start_date)
);

CREATE TABLE IF NOT EXISTS reconciliation_lines (
    id BIGSERIAL PRIMARY KEY,
    reconciliation_id BIGINT NOT NULL,
    transaction_date DATE NOT NULL,
    description VARCHAR(500),
    amount NUMERIC(19, 2) NOT NULL,
    transaction_type VARCHAR(20) NOT NULL CHECK (transaction_type IN ('BANK_STATEMENT', 'SYSTEM_TRANSACTION')),
    matched BOOLEAN NOT NULL DEFAULT FALSE,
    matched_transaction_id BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_reconciliation_lines_reconciliation FOREIGN KEY (reconciliation_id) REFERENCES reconciliations (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS budgets (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL,
    budget_year INTEGER NOT NULL,
    budget_month INTEGER,
    planned_amount NUMERIC(19, 2) NOT NULL,
    actual_amount NUMERIC(19, 2) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'APPROVED', 'ACTIVE', 'CLOSED')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT fk_budgets_account FOREIGN KEY (account_id) REFERENCES accounts (id),
    CONSTRAINT chk_budgets_month CHECK (budget_month IS NULL OR (budget_month >= 1 AND budget_month <= 12)),
    CONSTRAINT chk_budgets_amounts CHECK (planned_amount >= 0 AND actual_amount >= 0)
);

CREATE TABLE IF NOT EXISTS fiscal_years (
    id BIGSERIAL PRIMARY KEY,
    year INTEGER NOT NULL UNIQUE,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    is_open BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT chk_fiscal_years_dates CHECK (end_date > start_date)
);

CREATE TABLE IF NOT EXISTS fiscal_periods (
    id BIGSERIAL PRIMARY KEY,
    fiscal_year_id BIGINT NOT NULL,
    period_name VARCHAR(50) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    is_open BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT fk_fiscal_periods_year FOREIGN KEY (fiscal_year_id) REFERENCES fiscal_years (id),
    CONSTRAINT chk_fiscal_periods_dates CHECK (end_date >= start_date)
);

CREATE TABLE IF NOT EXISTS accounting_settings (
    id BIGSERIAL PRIMARY KEY,
    setting_key VARCHAR(100) NOT NULL UNIQUE,
    setting_value VARCHAR(500),
    description VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS numbering_sequences (
    id BIGSERIAL PRIMARY KEY,
    sequence_name VARCHAR(50) NOT NULL UNIQUE,
    prefix VARCHAR(20),
    current_number BIGINT NOT NULL DEFAULT 0,
    padding_length INTEGER NOT NULL DEFAULT 4,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by VARCHAR(100)
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_accounts_parent ON accounts (parent_id);
CREATE INDEX IF NOT EXISTS idx_accounts_type ON accounts (account_type);
CREATE INDEX IF NOT EXISTS idx_accounts_active ON accounts (is_active);
CREATE INDEX IF NOT EXISTS idx_journal_entries_date ON journal_entries (entry_date);
CREATE INDEX IF NOT EXISTS idx_journal_entries_status ON journal_entries (status);
CREATE INDEX IF NOT EXISTS idx_journal_entries_reference ON journal_entries (reference);
CREATE INDEX IF NOT EXISTS idx_journal_entry_lines_entry ON journal_entry_lines (journal_entry_id);
CREATE INDEX IF NOT EXISTS idx_journal_entry_lines_account ON journal_entry_lines (account_id);
CREATE INDEX IF NOT EXISTS idx_payment_vouchers_date ON payment_vouchers (voucher_date);
CREATE INDEX IF NOT EXISTS idx_payment_vouchers_status ON payment_vouchers (status);
CREATE INDEX IF NOT EXISTS idx_payment_vouchers_account ON payment_vouchers (account_id);
CREATE INDEX IF NOT EXISTS idx_receipt_vouchers_date ON receipt_vouchers (voucher_date);
CREATE INDEX IF NOT EXISTS idx_receipt_vouchers_status ON receipt_vouchers (status);
CREATE INDEX IF NOT EXISTS idx_receipt_vouchers_account ON receipt_vouchers (account_id);
CREATE INDEX IF NOT EXISTS idx_transfers_date ON transfers (transfer_date);
CREATE INDEX IF NOT EXISTS idx_transfers_status ON transfers (status);
CREATE INDEX IF NOT EXISTS idx_transactions_date ON transactions (transaction_date);
CREATE INDEX IF NOT EXISTS idx_transactions_type ON transactions (transaction_type);
CREATE INDEX IF NOT EXISTS idx_bills_date ON bills (bill_date);
CREATE INDEX IF NOT EXISTS idx_bills_status ON bills (status);
CREATE INDEX IF NOT EXISTS idx_bills_supplier ON bills (supplier_id);
CREATE INDEX IF NOT EXISTS idx_checks_type ON checks (check_type);
CREATE INDEX IF NOT EXISTS idx_checks_status ON checks (status);
CREATE INDEX IF NOT EXISTS idx_bank_accounts_active ON bank_accounts (is_active);
CREATE INDEX IF NOT EXISTS idx_reconciliations_bank ON reconciliations (bank_account_id);
CREATE INDEX IF NOT EXISTS idx_reconciliations_status ON reconciliations (status);
CREATE INDEX IF NOT EXISTS idx_budgets_account ON budgets (account_id);
CREATE INDEX IF NOT EXISTS idx_budgets_year ON budgets (budget_year);
CREATE INDEX IF NOT EXISTS idx_fiscal_years_year ON fiscal_years (year);
CREATE INDEX IF NOT EXISTS idx_fiscal_periods_year ON fiscal_periods (fiscal_year_id);

-- Insert default settings
INSERT INTO accounting_settings (setting_key, setting_value, description) VALUES
('ACCOUNTING_METHOD', 'ACCRUAL', 'Accounting method: ACCRUAL or CASH'),
('BASE_CURRENCY', 'USD', 'Base currency for the system'),
('ALLOW_MULTI_CURRENCY', 'false', 'Allow multi-currency transactions'),
('FISCAL_YEAR_START_MONTH', '1', 'Fiscal year start month (1-12)'),
('AUTO_GENERATE_ACCOUNT_CODE', 'true', 'Auto-generate account codes'),
('REQUIRE_APPROVAL_FOR_PAYMENTS', 'true', 'Require approval before posting payments'),
('REQUIRE_APPROVAL_FOR_RECEIPTS', 'true', 'Require approval before posting receipts')
ON CONFLICT (setting_key) DO NOTHING;

-- Insert numbering sequences
INSERT INTO numbering_sequences (sequence_name, prefix, current_number, padding_length) VALUES
('ACCOUNT_CODE', 'ACC', 1000, 4),
('JOURNAL_REFERENCE', 'JE', 1, 6),
('PAYMENT_VOUCHER', 'PV', 1, 6),
('RECEIPT_VOUCHER', 'RV', 1, 6),
('TRANSFER_REFERENCE', 'TR', 1, 6),
('BILL_NUMBER', 'BILL', 1, 6),
('CHECK_NUMBER', 'CHK', 1, 6)
ON CONFLICT (sequence_name) DO NOTHING;


-- ---------- source: V5__journal_entries_tables.sql ----------
SET search_path TO erp_system, public;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'erp_system'
          AND table_name = 'journal_entries'
          AND column_name = 'reference'
    ) AND NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'erp_system'
          AND table_name = 'journal_entries'
          AND column_name = 'reference_number'
    ) THEN
        ALTER TABLE journal_entries RENAME COLUMN reference TO reference_number;
    END IF;
END $$;

ALTER TABLE journal_entries
    ADD COLUMN IF NOT EXISTS reference_number VARCHAR(50),
    ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    ADD COLUMN IF NOT EXISTS total_debit NUMERIC(19, 4) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS total_credit NUMERIC(19, 4) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS posted_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS posted_by VARCHAR(100),
    ADD COLUMN IF NOT EXISTS reversed_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS reversed_by VARCHAR(100),
    ADD COLUMN IF NOT EXISTS reversal_reference VARCHAR(50),
    ADD COLUMN IF NOT EXISTS source_module VARCHAR(40),
    ADD COLUMN IF NOT EXISTS source_record_id BIGINT;

UPDATE journal_entries
SET reference_number = COALESCE(reference_number, 'JE-' || id)
WHERE reference_number IS NULL;

ALTER TABLE journal_entries
    ALTER COLUMN reference_number SET NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'uq_journal_entries_reference_number'
          AND conrelid = 'erp_system.journal_entries'::regclass
    ) THEN
        ALTER TABLE journal_entries
            ADD CONSTRAINT uq_journal_entries_reference_number UNIQUE (reference_number);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_journal_entries_status'
          AND conrelid = 'erp_system.journal_entries'::regclass
    ) THEN
        ALTER TABLE journal_entries
            ADD CONSTRAINT chk_journal_entries_status
                CHECK (status IN ('DRAFT', 'POSTED', 'REVERSED', 'CANCELLED'));
    END IF;
END $$;

ALTER TABLE journal_entry_lines
    ADD COLUMN IF NOT EXISTS line_number INTEGER;

WITH numbered_lines AS (
    SELECT id,
           ROW_NUMBER() OVER (PARTITION BY journal_entry_id ORDER BY id) AS generated_line_number
    FROM journal_entry_lines
)
UPDATE journal_entry_lines line
SET line_number = numbered_lines.generated_line_number
FROM numbered_lines
WHERE line.id = numbered_lines.id
  AND line.line_number IS NULL;

ALTER TABLE journal_entry_lines
    ALTER COLUMN line_number SET NOT NULL;

UPDATE journal_entries header
SET total_debit = balances.total_debit,
    total_credit = balances.total_credit
FROM (
    SELECT journal_entry_id,
           COALESCE(SUM(debit), 0) AS total_debit,
           COALESCE(SUM(credit), 0) AS total_credit
    FROM journal_entry_lines
    GROUP BY journal_entry_id
) balances
WHERE header.id = balances.journal_entry_id;

CREATE INDEX IF NOT EXISTS idx_journal_entries_reference_number ON journal_entries(reference_number);
CREATE INDEX IF NOT EXISTS idx_journal_entries_status ON journal_entries(status);
CREATE INDEX IF NOT EXISTS idx_journal_entries_source ON journal_entries(source_module, source_record_id);
CREATE INDEX IF NOT EXISTS idx_journal_entry_lines_line_number ON journal_entry_lines(journal_entry_id, line_number);


-- ---------- source: V6__accounting_module_extensions.sql ----------
SET search_path TO erp_system, public;

ALTER TABLE payment_vouchers
    ADD COLUMN IF NOT EXISTS party_name VARCHAR(150),
    ADD COLUMN IF NOT EXISTS linked_document_reference VARCHAR(80),
    ADD COLUMN IF NOT EXISTS approved_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS approved_by VARCHAR(100),
    ADD COLUMN IF NOT EXISTS reversal_journal_entry_id BIGINT;

ALTER TABLE receipt_vouchers
    ADD COLUMN IF NOT EXISTS party_name VARCHAR(150),
    ADD COLUMN IF NOT EXISTS invoice_reference VARCHAR(80),
    ADD COLUMN IF NOT EXISTS approved_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS approved_by VARCHAR(100),
    ADD COLUMN IF NOT EXISTS reversal_journal_entry_id BIGINT;

ALTER TABLE transfers
    ADD COLUMN IF NOT EXISTS journal_entry_id BIGINT,
    ADD COLUMN IF NOT EXISTS reversal_journal_entry_id BIGINT;

ALTER TABLE transactions
    ADD COLUMN IF NOT EXISTS debit_account_id BIGINT,
    ADD COLUMN IF NOT EXISTS credit_account_id BIGINT,
    ADD COLUMN IF NOT EXISTS journal_entry_id BIGINT,
    ADD COLUMN IF NOT EXISTS original_transaction_id BIGINT,
    ADD COLUMN IF NOT EXISTS related_document_reference VARCHAR(80),
    ADD COLUMN IF NOT EXISTS posted_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS posted_by VARCHAR(100);

ALTER TABLE bills
    ADD COLUMN IF NOT EXISTS supplier_name VARCHAR(150),
    ADD COLUMN IF NOT EXISTS supplier_reference VARCHAR(80),
    ADD COLUMN IF NOT EXISTS payable_account_id BIGINT,
    ADD COLUMN IF NOT EXISTS tax_account_id BIGINT,
    ADD COLUMN IF NOT EXISTS journal_entry_id BIGINT,
    ADD COLUMN IF NOT EXISTS cancellation_journal_entry_id BIGINT,
    ADD COLUMN IF NOT EXISTS approved_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS approved_by VARCHAR(100),
    ADD COLUMN IF NOT EXISTS cancelled_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS cancelled_by VARCHAR(100);

ALTER TABLE bills
    ALTER COLUMN supplier_id DROP NOT NULL;

ALTER TABLE bill_lines
    ADD COLUMN IF NOT EXISTS created_by VARCHAR(100),
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(100);

ALTER TABLE checks
    ADD COLUMN IF NOT EXISTS bank_account_id BIGINT,
    ADD COLUMN IF NOT EXISTS holding_account_id BIGINT,
    ADD COLUMN IF NOT EXISTS journal_entry_id BIGINT,
    ADD COLUMN IF NOT EXISTS reversal_journal_entry_id BIGINT,
    ADD COLUMN IF NOT EXISTS linked_document_reference VARCHAR(80),
    ADD COLUMN IF NOT EXISTS cleared_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS bounced_at TIMESTAMPTZ;

ALTER TABLE reconciliation_lines
    ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'UNMATCHED',
    ADD COLUMN IF NOT EXISTS source_reference VARCHAR(80),
    ADD COLUMN IF NOT EXISTS journal_entry_line_id BIGINT,
    ADD COLUMN IF NOT EXISTS matched_line_id BIGINT,
    ADD COLUMN IF NOT EXISTS matched_amount NUMERIC(19, 2);

ALTER TABLE budgets
    ADD COLUMN IF NOT EXISTS budget_name VARCHAR(150),
    ADD COLUMN IF NOT EXISTS notes VARCHAR(500);

ALTER TABLE fiscal_years
    ADD COLUMN IF NOT EXISTS closed_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS closed_by VARCHAR(100);

ALTER TABLE fiscal_periods
    ADD COLUMN IF NOT EXISTS closed_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS closed_by VARCHAR(100);

ALTER TABLE accounting_settings
    ADD COLUMN IF NOT EXISTS created_by VARCHAR(100);

ALTER TABLE numbering_sequences
    ADD COLUMN IF NOT EXISTS created_by VARCHAR(100);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_payment_vouchers_reversal_entry'
          AND conrelid = 'erp_system.payment_vouchers'::regclass
    ) THEN
        ALTER TABLE payment_vouchers
            ADD CONSTRAINT fk_payment_vouchers_reversal_entry
                FOREIGN KEY (reversal_journal_entry_id) REFERENCES journal_entries (id);
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_receipt_vouchers_reversal_entry'
          AND conrelid = 'erp_system.receipt_vouchers'::regclass
    ) THEN
        ALTER TABLE receipt_vouchers
            ADD CONSTRAINT fk_receipt_vouchers_reversal_entry
                FOREIGN KEY (reversal_journal_entry_id) REFERENCES journal_entries (id);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_transfers_journal_entry'
          AND conrelid = 'erp_system.transfers'::regclass
    ) THEN
        ALTER TABLE transfers
            ADD CONSTRAINT fk_transfers_journal_entry
                FOREIGN KEY (journal_entry_id) REFERENCES journal_entries (id);
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_transfers_reversal_entry'
          AND conrelid = 'erp_system.transfers'::regclass
    ) THEN
        ALTER TABLE transfers
            ADD CONSTRAINT fk_transfers_reversal_entry
                FOREIGN KEY (reversal_journal_entry_id) REFERENCES journal_entries (id);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_transactions_debit_account'
          AND conrelid = 'erp_system.transactions'::regclass
    ) THEN
        ALTER TABLE transactions
            ADD CONSTRAINT fk_transactions_debit_account
                FOREIGN KEY (debit_account_id) REFERENCES accounts (id);
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_transactions_credit_account'
          AND conrelid = 'erp_system.transactions'::regclass
    ) THEN
        ALTER TABLE transactions
            ADD CONSTRAINT fk_transactions_credit_account
                FOREIGN KEY (credit_account_id) REFERENCES accounts (id);
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_transactions_journal_entry'
          AND conrelid = 'erp_system.transactions'::regclass
    ) THEN
        ALTER TABLE transactions
            ADD CONSTRAINT fk_transactions_journal_entry
                FOREIGN KEY (journal_entry_id) REFERENCES journal_entries (id);
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_transactions_original_transaction'
          AND conrelid = 'erp_system.transactions'::regclass
    ) THEN
        ALTER TABLE transactions
            ADD CONSTRAINT fk_transactions_original_transaction
                FOREIGN KEY (original_transaction_id) REFERENCES transactions (id);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_bills_payable_account'
          AND conrelid = 'erp_system.bills'::regclass
    ) THEN
        ALTER TABLE bills
            ADD CONSTRAINT fk_bills_payable_account
                FOREIGN KEY (payable_account_id) REFERENCES accounts (id);
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_bills_tax_account'
          AND conrelid = 'erp_system.bills'::regclass
    ) THEN
        ALTER TABLE bills
            ADD CONSTRAINT fk_bills_tax_account
                FOREIGN KEY (tax_account_id) REFERENCES accounts (id);
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_bills_journal_entry'
          AND conrelid = 'erp_system.bills'::regclass
    ) THEN
        ALTER TABLE bills
            ADD CONSTRAINT fk_bills_journal_entry
                FOREIGN KEY (journal_entry_id) REFERENCES journal_entries (id);
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_bills_cancellation_entry'
          AND conrelid = 'erp_system.bills'::regclass
    ) THEN
        ALTER TABLE bills
            ADD CONSTRAINT fk_bills_cancellation_entry
                FOREIGN KEY (cancellation_journal_entry_id) REFERENCES journal_entries (id);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_checks_bank_account'
          AND conrelid = 'erp_system.checks'::regclass
    ) THEN
        ALTER TABLE checks
            ADD CONSTRAINT fk_checks_bank_account
                FOREIGN KEY (bank_account_id) REFERENCES bank_accounts (id);
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_checks_holding_account'
          AND conrelid = 'erp_system.checks'::regclass
    ) THEN
        ALTER TABLE checks
            ADD CONSTRAINT fk_checks_holding_account
                FOREIGN KEY (holding_account_id) REFERENCES accounts (id);
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_checks_journal_entry'
          AND conrelid = 'erp_system.checks'::regclass
    ) THEN
        ALTER TABLE checks
            ADD CONSTRAINT fk_checks_journal_entry
                FOREIGN KEY (journal_entry_id) REFERENCES journal_entries (id);
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_checks_reversal_entry'
          AND conrelid = 'erp_system.checks'::regclass
    ) THEN
        ALTER TABLE checks
            ADD CONSTRAINT fk_checks_reversal_entry
                FOREIGN KEY (reversal_journal_entry_id) REFERENCES journal_entries (id);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'chk_reconciliation_lines_status'
          AND conrelid = 'erp_system.reconciliation_lines'::regclass
    ) THEN
        ALTER TABLE reconciliation_lines
            ADD CONSTRAINT chk_reconciliation_lines_status
                CHECK (status IN ('UNMATCHED', 'PARTIALLY_MATCHED', 'MATCHED'));
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_payment_vouchers_bill_id ON payment_vouchers(bill_id);
CREATE INDEX IF NOT EXISTS idx_payment_vouchers_method ON payment_vouchers(payment_method);
CREATE INDEX IF NOT EXISTS idx_receipt_vouchers_method ON receipt_vouchers(payment_method);
CREATE INDEX IF NOT EXISTS idx_transfers_journal_entry ON transfers(journal_entry_id);
CREATE INDEX IF NOT EXISTS idx_transactions_status ON transactions(status);
CREATE INDEX IF NOT EXISTS idx_transactions_document_reference ON transactions(related_document_reference);
CREATE INDEX IF NOT EXISTS idx_bills_journal_entry ON bills(journal_entry_id);
CREATE INDEX IF NOT EXISTS idx_checks_bank_account ON checks(bank_account_id);
CREATE INDEX IF NOT EXISTS idx_reconciliation_lines_status ON reconciliation_lines(status);
CREATE INDEX IF NOT EXISTS idx_reconciliation_lines_journal_line ON reconciliation_lines(journal_entry_line_id);

INSERT INTO numbering_sequences (sequence_name, prefix, current_number, padding_length)
VALUES
    ('TRANSACTION_REFERENCE', 'TXN-', 1, 6),
    ('BUDGET_REFERENCE', 'BDG-', 1, 6),
    ('BANK_RECONCILIATION', 'REC-', 1, 6)
ON CONFLICT (sequence_name) DO NOTHING;


-- ---------- source: V7__accounting_runtime_alignment.sql ----------
SET search_path TO erp_system, public;

ALTER TABLE reconciliation_lines
    ADD COLUMN IF NOT EXISTS created_by VARCHAR(100),
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(100);


-- ---------- source: V8__auth_audit_alignment.sql ----------
SET search_path TO erp_system, public;

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS created_by VARCHAR(100),
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(100);

ALTER TABLE user_profiles
    ADD COLUMN IF NOT EXISTS created_by VARCHAR(100),
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(100);


-- ---------- source: V9__customer_invoice_tables.sql ----------
-- V9__customer_invoice_tables.sql
-- Add customer invoices and invoice lines tables

SET search_path TO erp_system, public;

CREATE TABLE IF NOT EXISTS customer_invoices (
    id BIGSERIAL PRIMARY KEY,
    invoice_number VARCHAR(50) NOT NULL UNIQUE,
    invoice_date DATE NOT NULL,
    due_date DATE NOT NULL,
    customer_name VARCHAR(150),
    customer_reference VARCHAR(80),
    description VARCHAR(500),
    subtotal NUMERIC(19, 2) NOT NULL DEFAULT 0,
    tax_amount NUMERIC(19, 2) NOT NULL DEFAULT 0,
    total_amount NUMERIC(19, 2) NOT NULL DEFAULT 0,
    paid_amount NUMERIC(19, 2) NOT NULL DEFAULT 0,
    outstanding_amount NUMERIC(19, 2) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'POSTED', 'PARTIAL', 'PAID', 'CANCELLED')),
    receivable_account_id BIGINT NOT NULL,
    revenue_account_id BIGINT NOT NULL,
    journal_entry_id BIGINT UNIQUE,
    cancellation_journal_entry_id BIGINT UNIQUE,
    posted_at TIMESTAMPTZ,
    posted_by VARCHAR(100),
    cancelled_at TIMESTAMPTZ,
    cancelled_by VARCHAR(100),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT fk_customer_invoices_receivable_account FOREIGN KEY (receivable_account_id) REFERENCES accounts (id),
    CONSTRAINT fk_customer_invoices_revenue_account FOREIGN KEY (revenue_account_id) REFERENCES accounts (id),
    CONSTRAINT fk_customer_invoices_journal_entry FOREIGN KEY (journal_entry_id) REFERENCES journal_entries (id),
    CONSTRAINT fk_customer_invoices_cancellation_journal_entry FOREIGN KEY (cancellation_journal_entry_id) REFERENCES journal_entries (id),
    CONSTRAINT chk_customer_invoices_dates CHECK (due_date >= invoice_date),
    CONSTRAINT chk_customer_invoices_amounts CHECK (
        subtotal >= 0 AND
        tax_amount >= 0 AND
        total_amount >= 0 AND
        paid_amount >= 0 AND
        outstanding_amount >= 0
    )
);

CREATE TABLE IF NOT EXISTS customer_invoice_lines (
    id BIGSERIAL PRIMARY KEY,
    invoice_id BIGINT NOT NULL,
    account_id BIGINT NOT NULL,
    description VARCHAR(500),
    quantity NUMERIC(10, 2) NOT NULL DEFAULT 1,
    unit_price NUMERIC(19, 2) NOT NULL DEFAULT 0,
    line_total NUMERIC(19, 2) NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT fk_customer_invoice_lines_invoice FOREIGN KEY (invoice_id) REFERENCES customer_invoices (id) ON DELETE CASCADE,
    CONSTRAINT fk_customer_invoice_lines_account FOREIGN KEY (account_id) REFERENCES accounts (id),
    CONSTRAINT chk_customer_invoice_lines_amounts CHECK (quantity > 0 AND unit_price >= 0 AND line_total >= 0)
);

CREATE INDEX IF NOT EXISTS idx_customer_invoices_date ON customer_invoices (invoice_date);
CREATE INDEX IF NOT EXISTS idx_customer_invoices_status ON customer_invoices (status);
CREATE INDEX IF NOT EXISTS idx_customer_invoices_receivable_account ON customer_invoices (receivable_account_id);
CREATE INDEX IF NOT EXISTS idx_customer_invoices_revenue_account ON customer_invoices (revenue_account_id);
CREATE INDEX IF NOT EXISTS idx_customer_invoice_lines_invoice ON customer_invoice_lines (invoice_id);
CREATE INDEX IF NOT EXISTS idx_customer_invoice_lines_account ON customer_invoice_lines (account_id);


-- ---------- source: V10__accounting_reference_seed.sql ----------
-- V10__accounting_reference_seed.sql
-- Seed reference data for dynamic accounting screens

SET search_path TO erp_system, public;

-- Ensure numbering sequences used by UI/API are present.
INSERT INTO numbering_sequences (sequence_name, prefix, current_number, padding_length)
VALUES
('TRANSACTION_REFERENCE', 'TX', 1, 6),
('CUSTOMER_INVOICE', 'INV', 1, 6),
('RECONCILIATION_REFERENCE', 'REC', 1, 6)
ON CONFLICT (sequence_name) DO NOTHING;

-- Seed a sample bank account tied to chart of accounts code 1120 when available.
INSERT INTO bank_accounts (
    bank_name,
    account_number,
    iban,
    currency,
    opening_balance,
    current_balance,
    linked_account_id,
    is_active,
    created_by,
    updated_by
)
SELECT
    'ERP Demo Bank',
    'ERP-0001',
    'SA0000000000000000000001',
    'USD',
    10000,
    10000,
    a.id,
    TRUE,
    'flyway',
    'flyway'
FROM accounts a
WHERE a.code = '1120'
ON CONFLICT (account_number) DO NOTHING;

-- Seed one customer invoice and line for dynamic invoice references.
WITH receivable AS (
    SELECT id FROM accounts WHERE code = '1100' LIMIT 1
),
revenue AS (
    SELECT id FROM accounts WHERE code = '4100' LIMIT 1
),
created_invoice AS (
    INSERT INTO customer_invoices (
        invoice_number,
        invoice_date,
        due_date,
        customer_name,
        customer_reference,
        description,
        subtotal,
        tax_amount,
        total_amount,
        paid_amount,
        outstanding_amount,
        status,
        receivable_account_id,
        revenue_account_id,
        created_by,
        updated_by
    )
    SELECT
        'INV-000001',
        CURRENT_DATE,
        CURRENT_DATE + INTERVAL '30 days',
        'Demo Customer',
        'CUST-001',
        'Seeded invoice for API-driven screens',
        1000,
        150,
        1150,
        0,
        1150,
        'DRAFT',
        receivable.id,
        revenue.id,
        'flyway',
        'flyway'
    FROM receivable, revenue
    ON CONFLICT (invoice_number) DO NOTHING
    RETURNING id
),
invoice_ref AS (
    SELECT id FROM created_invoice
    UNION ALL
    SELECT id FROM customer_invoices WHERE invoice_number = 'INV-000001'
)
INSERT INTO customer_invoice_lines (
    invoice_id,
    account_id,
    description,
    quantity,
    unit_price,
    line_total,
    created_by,
    updated_by
)
SELECT
    invoice_ref.id,
    revenue.id,
    'Seeded invoice line',
    1,
    1000,
    1000,
    'flyway',
    'flyway'
FROM invoice_ref, revenue
WHERE NOT EXISTS (
    SELECT 1
    FROM customer_invoice_lines l
    WHERE l.invoice_id = invoice_ref.id
);


-- ---------- source: V11__admin_password_bcrypt_repair.sql ----------
SET search_path TO erp_system, public;

-- Repair databases where `admin` password was set to plaintext (Spring expects BCrypt).
-- After this migration: username `admin`, email `admin@erp.local`, password `Admin@123`
UPDATE users
SET password = '$2b$10$6CWuV2VRnCMQwvRzQE6LQu7SAHTflMSv6IvQbUfJhE4y.GktFGXiW',
    updated_at   = NOW()
WHERE username = 'admin'
  AND password NOT LIKE '$2%';


-- ---------- source: V12__journal_description_as_text.sql ----------
-- Fix journal entry description stored as BYTEA (breaks LOWER() in JPQL search on PostgreSQL).
SET search_path TO erp_system, public;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'erp_system'
          AND table_name = 'journal_entries'
          AND column_name = 'description'
          AND udt_name = 'bytea'
    ) THEN
        ALTER TABLE journal_entries
            ALTER COLUMN description TYPE VARCHAR(1000)
                USING (
                    CASE
                        WHEN description IS NULL THEN NULL
                        WHEN octet_length(description) = 0 THEN NULL
                        ELSE substring(convert_from(description, 'UTF8') FROM 1 FOR 1000)
                    END
                );
    END IF;

    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'erp_system'
          AND table_name = 'journal_entry_lines'
          AND column_name = 'description'
          AND udt_name = 'bytea'
    ) THEN
        ALTER TABLE journal_entry_lines
            ALTER COLUMN description TYPE VARCHAR(1000)
                USING (
                    CASE
                        WHEN description IS NULL THEN NULL
                        WHEN octet_length(description) = 0 THEN NULL
                        ELSE substring(convert_from(description, 'UTF8') FROM 1 FOR 1000)
                    END
                );
    END IF;
END $$;


-- ---------- source: V13__ui_navigation_menu.sql ----------
SET search_path TO erp_system, public;

CREATE TABLE IF NOT EXISTS ui_menu_items (
    id VARCHAR(64) PRIMARY KEY,
    parent_id VARCHAR(64) REFERENCES erp_system.ui_menu_items (id) ON DELETE CASCADE,
    sort_order INT NOT NULL DEFAULT 0,
    item_type VARCHAR(16) NOT NULL,
    title_key VARCHAR(128) NOT NULL,
    icon VARCHAR(64),
    url VARCHAR(512),
    is_external BOOLEAN NOT NULL DEFAULT FALSE,
    target_blank BOOLEAN NOT NULL DEFAULT FALSE,
    roles_csv VARCHAR(256),
    item_classes VARCHAR(128),
    breadcrumbs_flag BOOLEAN
);

CREATE INDEX IF NOT EXISTS idx_ui_menu_items_parent ON erp_system.ui_menu_items (parent_id);
CREATE INDEX IF NOT EXISTS idx_ui_menu_items_sort ON erp_system.ui_menu_items (parent_id, sort_order);

INSERT INTO erp_system.ui_menu_items (id, parent_id, sort_order, item_type, title_key, icon, url, is_external, target_blank, roles_csv, item_classes, breadcrumbs_flag)
VALUES
    ('dashboard', NULL, 0, 'group', 'NAV.DASHBOARD_GROUP', 'menu', NULL, FALSE, FALSE, 'ADMIN,USER', NULL, NULL),
    ('default', 'dashboard', 0, 'item', 'NAV.DASHBOARD_DEFAULT', 'dashboard', '/dashboard/default', FALSE, FALSE, NULL, 'nav-item', FALSE),

    ('accounting', NULL, 1, 'group', 'ACCOUNTING.TITLE', 'account_balance_wallet', NULL, FALSE, FALSE, 'ADMIN', NULL, NULL),
    ('accounting-dashboard', 'accounting', 0, 'item', 'ACCOUNTING.DASHBOARD', 'dashboard', '/accounting/dashboard', FALSE, FALSE, NULL, 'nav-item', FALSE),
    ('chart-of-accounts', 'accounting', 1, 'item', 'ACCOUNTING.CHART_OF_ACCOUNTS', 'account_tree', '/accounting/chart-of-accounts', FALSE, FALSE, NULL, 'nav-item', NULL),
    ('ledger', 'accounting', 2, 'item', 'ACCOUNTING.LEDGER', 'menu_book', '/accounting/ledger', FALSE, FALSE, NULL, 'nav-item', NULL),
    ('journal-entries', 'accounting', 3, 'item', 'ACCOUNTING.JOURNAL_ENTRIES', 'receipt', '/accounting/journal-entries', FALSE, FALSE, NULL, 'nav-item', NULL),
    ('payment-vouchers', 'accounting', 4, 'item', 'ACCOUNTING.PAYMENT_VOUCHERS', 'payment', '/accounting/payment-vouchers', FALSE, FALSE, NULL, 'nav-item', NULL),
    ('receipt-vouchers', 'accounting', 5, 'item', 'ACCOUNTING.RECEIPT_VOUCHERS', 'receipt_long', '/accounting/receipt-vouchers', FALSE, FALSE, NULL, 'nav-item', NULL),
    ('transfers', 'accounting', 6, 'item', 'ACCOUNTING.TRANSFERS', 'swap_horiz', '/accounting/transfers', FALSE, FALSE, NULL, 'nav-item', NULL),
    ('transactions', 'accounting', 7, 'item', 'ACCOUNTING.TRANSACTIONS', 'list_alt', '/accounting/transactions', FALSE, FALSE, NULL, 'nav-item', NULL),
    ('bills', 'accounting', 8, 'item', 'ACCOUNTING.BILLS', 'description', '/accounting/bills', FALSE, FALSE, NULL, 'nav-item', NULL),
    ('checks', 'accounting', 9, 'item', 'ACCOUNTING.CHECKS', 'check', '/accounting/checks', FALSE, FALSE, NULL, 'nav-item', NULL),
    ('bank-accounts', 'accounting', 10, 'item', 'ACCOUNTING.BANK_ACCOUNTS', 'account_balance', '/accounting/bank-accounts', FALSE, FALSE, NULL, 'nav-item', NULL),
    ('registers', 'accounting', 11, 'item', 'ACCOUNTING.REGISTERS', 'table_chart', '/accounting/registers', FALSE, FALSE, NULL, 'nav-item', NULL),
    ('reconciliation', 'accounting', 12, 'item', 'ACCOUNTING.RECONCILIATION', 'compare_arrows', '/accounting/reconciliation', FALSE, FALSE, NULL, 'nav-item', NULL),
    ('budget', 'accounting', 13, 'item', 'ACCOUNTING.BUDGET', 'pie_chart', '/accounting/budget', FALSE, FALSE, NULL, 'nav-item', NULL),
    ('settings', 'accounting', 14, 'item', 'ACCOUNTING.SETTINGS', 'settings', '/accounting/settings', FALSE, FALSE, NULL, 'nav-item', NULL),

    ('authentication', NULL, 2, 'group', 'NAV.AUTH_GROUP', 'lock', NULL, FALSE, FALSE, 'ADMIN,USER', NULL, NULL),
    ('login', 'authentication', 0, 'item', 'NAV.LOGIN', 'login', '/login', FALSE, TRUE, NULL, 'nav-item', FALSE),
    ('register', 'authentication', 1, 'item', 'NAV.REGISTER', 'profile', '/register', FALSE, TRUE, NULL, 'nav-item', FALSE),

    ('utilities', NULL, 3, 'group', 'NAV.UI_COMPONENTS', 'widgets', NULL, FALSE, FALSE, 'ADMIN,USER', NULL, NULL),
    ('typography', 'utilities', 0, 'item', 'NAV.TYPOGRAPHY', 'format_size', '/typography', FALSE, FALSE, NULL, 'nav-item', NULL),
    ('color', 'utilities', 1, 'item', 'NAV.COLORS', 'palette', '/color', FALSE, FALSE, NULL, 'nav-item', NULL),
    ('ant-icons', 'utilities', 2, 'item', 'NAV.ANT_ICONS', 'apps', 'https://ant.design/components/icon', TRUE, TRUE, NULL, 'nav-item', NULL),

    ('other', NULL, 4, 'group', 'NAV.OTHER', 'more_horiz', NULL, FALSE, FALSE, 'ADMIN,USER', NULL, NULL),
    ('sample-page', 'other', 0, 'item', 'NAV.SAMPLE_PAGE', 'article', '/sample-page', FALSE, FALSE, NULL, 'nav-item', NULL),
    ('document', 'other', 1, 'item', 'NAV.DOCUMENT', 'help_outline', 'https://codedthemes.gitbook.io/mantis-angular/', TRUE, TRUE, NULL, 'nav-item', NULL);


-- ---------- source: V14__journal_entry_header_fields.sql ----------
SET search_path TO erp_system, public;

ALTER TABLE journal_entries ADD COLUMN IF NOT EXISTS external_reference VARCHAR(80);
ALTER TABLE journal_entries ADD COLUMN IF NOT EXISTS currency_code VARCHAR(3) NOT NULL DEFAULT 'USD';
ALTER TABLE journal_entries ADD COLUMN IF NOT EXISTS entry_type VARCHAR(30) NOT NULL DEFAULT 'MANUAL';


-- ---------- source: V15__lookup_tables_and_values.sql ----------
SET search_path TO erp_system, public;

CREATE TABLE IF NOT EXISTS lookup_values (
    id BIGSERIAL PRIMARY KEY,
    type_code VARCHAR(60) NOT NULL,
    code VARCHAR(80) NOT NULL,
    sort_order INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT uq_lookup_values_type_code UNIQUE (type_code, code)
);

CREATE INDEX IF NOT EXISTS idx_lookup_values_type ON lookup_values(type_code, is_active, sort_order);

INSERT INTO lookup_values (type_code, code, sort_order, is_active, created_by, updated_by)
VALUES
('account-types', 'ASSET', 1, TRUE, 'flyway', 'flyway'),
('account-types', 'LIABILITY', 2, TRUE, 'flyway', 'flyway'),
('account-types', 'EQUITY', 3, TRUE, 'flyway', 'flyway'),
('account-types', 'INCOME', 4, TRUE, 'flyway', 'flyway'),
('account-types', 'EXPENSE', 5, TRUE, 'flyway', 'flyway'),
('voucher-statuses', 'DRAFT', 1, TRUE, 'flyway', 'flyway'),
('voucher-statuses', 'APPROVED', 2, TRUE, 'flyway', 'flyway'),
('voucher-statuses', 'POSTED', 3, TRUE, 'flyway', 'flyway'),
('voucher-statuses', 'CANCELLED', 4, TRUE, 'flyway', 'flyway'),
('voucher-types', 'STANDARD', 1, TRUE, 'flyway', 'flyway'),
('voucher-types', 'ADVANCE', 2, TRUE, 'flyway', 'flyway'),
('voucher-types', 'BILL_PAYMENT', 3, TRUE, 'flyway', 'flyway'),
('voucher-types', 'INVOICE_COLLECTION', 4, TRUE, 'flyway', 'flyway'),
('payment-methods', 'CASH', 1, TRUE, 'flyway', 'flyway'),
('payment-methods', 'BANK', 2, TRUE, 'flyway', 'flyway'),
('payment-methods', 'CHECK', 3, TRUE, 'flyway', 'flyway'),
('currencies', 'USD', 1, TRUE, 'flyway', 'flyway'),
('currencies', 'EUR', 2, TRUE, 'flyway', 'flyway'),
('currencies', 'GBP', 3, TRUE, 'flyway', 'flyway'),
('currencies', 'AED', 4, TRUE, 'flyway', 'flyway'),
('currencies', 'SAR', 5, TRUE, 'flyway', 'flyway'),
('currencies', 'EGP', 6, TRUE, 'flyway', 'flyway'),
('reconciliation-statuses', 'OPEN', 1, TRUE, 'flyway', 'flyway'),
('reconciliation-statuses', 'IN_PROGRESS', 2, TRUE, 'flyway', 'flyway'),
('reconciliation-statuses', 'COMPLETED', 3, TRUE, 'flyway', 'flyway'),
('reconciliation-line-statuses', 'UNMATCHED', 1, TRUE, 'flyway', 'flyway'),
('reconciliation-line-statuses', 'PARTIALLY_MATCHED', 2, TRUE, 'flyway', 'flyway'),
('reconciliation-line-statuses', 'MATCHED', 3, TRUE, 'flyway', 'flyway'),
('report-periods', 'THIS_MONTH', 1, TRUE, 'flyway', 'flyway'),
('report-periods', 'LAST_MONTH', 2, TRUE, 'flyway', 'flyway'),
('report-periods', 'THIS_QUARTER', 3, TRUE, 'flyway', 'flyway'),
('report-periods', 'THIS_YEAR', 4, TRUE, 'flyway', 'flyway'),
('report-periods', 'CUSTOM', 5, TRUE, 'flyway', 'flyway'),
('journal-entry-statuses', 'DRAFT', 1, TRUE, 'flyway', 'flyway'),
('journal-entry-statuses', 'POSTED', 2, TRUE, 'flyway', 'flyway'),
('journal-entry-statuses', 'REVERSED', 3, TRUE, 'flyway', 'flyway'),
('entry-types', 'MANUAL', 1, TRUE, 'flyway', 'flyway'),
('entry-types', 'ADJUSTMENT', 2, TRUE, 'flyway', 'flyway'),
('entry-types', 'OPENING', 3, TRUE, 'flyway', 'flyway'),
('entry-types', 'CLOSING', 4, TRUE, 'flyway', 'flyway'),
('entry-types', 'REVERSAL', 5, TRUE, 'flyway', 'flyway')
ON CONFLICT (type_code, code) DO NOTHING;


-- ---------- source: V16__voucher_currency_and_type.sql ----------
SET search_path TO erp_system, public;

ALTER TABLE payment_vouchers
    ADD COLUMN IF NOT EXISTS currency_code VARCHAR(3),
    ADD COLUMN IF NOT EXISTS voucher_type VARCHAR(30);

ALTER TABLE receipt_vouchers
    ADD COLUMN IF NOT EXISTS currency_code VARCHAR(3),
    ADD COLUMN IF NOT EXISTS voucher_type VARCHAR(30);

UPDATE payment_vouchers
SET currency_code = COALESCE(currency_code, 'USD'),
    voucher_type = COALESCE(voucher_type, 'STANDARD')
WHERE currency_code IS NULL
   OR voucher_type IS NULL;

UPDATE receipt_vouchers
SET currency_code = COALESCE(currency_code, 'USD'),
    voucher_type = COALESCE(voucher_type, 'STANDARD')
WHERE currency_code IS NULL
   OR voucher_type IS NULL;

ALTER TABLE payment_vouchers
    ALTER COLUMN currency_code SET NOT NULL,
    ALTER COLUMN voucher_type SET NOT NULL;

ALTER TABLE receipt_vouchers
    ALTER COLUMN currency_code SET NOT NULL,
    ALTER COLUMN voucher_type SET NOT NULL;


-- ---------- source: V17__enterprise_accounting_demo_seed.sql ----------
SET search_path TO erp_system, public;

-- Chart of accounts (enterprise demo set)
-- Keep compatibility with legacy schema where "name" is still NOT NULL.
INSERT INTO accounts (code, name, name_en, name_ar, account_type, level, full_path, is_active, is_postable, opening_balance, opening_balance_side, created_by, updated_by)
VALUES
('1000', 'Assets', 'Assets', 'Ø§Ù„Ø£ØµÙˆÙ„', 'ASSET', 1, 'Assets', TRUE, FALSE, 0, 'DEBIT', 'flyway', 'flyway'),
('1100', 'Cash and Cash Equivalents', 'Cash and Cash Equivalents', 'Ø§Ù„Ù†Ù‚Ø¯ ÙˆÙ…Ø§ ÙÙŠ Ø­ÙƒÙ…Ù‡', 'ASSET', 2, 'Assets/Cash and Cash Equivalents', TRUE, FALSE, 0, 'DEBIT', 'flyway', 'flyway'),
('1110', 'Cash on Hand', 'Cash on Hand', 'Ø§Ù„ØµÙ†Ø¯ÙˆÙ‚', 'ASSET', 3, 'Assets/Cash and Cash Equivalents/Cash on Hand', TRUE, TRUE, 2500.00, 'DEBIT', 'flyway', 'flyway'),
('1120', 'Main Bank Account', 'Main Bank Account', 'Ø§Ù„Ø­Ø³Ø§Ø¨ Ø§Ù„Ø¨Ù†ÙƒÙŠ Ø§Ù„Ø±Ø¦ÙŠØ³ÙŠ', 'ASSET', 3, 'Assets/Cash and Cash Equivalents/Main Bank Account', TRUE, TRUE, 35000.00, 'DEBIT', 'flyway', 'flyway'),
('1130', 'Savings Bank Account', 'Savings Bank Account', 'Ø­Ø³Ø§Ø¨ Ø§Ù„Ø¨Ù†Ùƒ Ø§Ù„Ø§Ø¯Ø®Ø§Ø±ÙŠ', 'ASSET', 3, 'Assets/Cash and Cash Equivalents/Savings Bank Account', TRUE, TRUE, 12000.00, 'DEBIT', 'flyway', 'flyway'),
('1200', 'Accounts Receivable', 'Accounts Receivable', 'Ø§Ù„Ø°Ù…Ù… Ø§Ù„Ù…Ø¯ÙŠÙ†Ø©', 'ASSET', 2, 'Assets/Accounts Receivable', TRUE, TRUE, 18000.00, 'DEBIT', 'flyway', 'flyway'),
('1300', 'Inventory', 'Inventory', 'Ø§Ù„Ù…Ø®Ø²ÙˆÙ†', 'ASSET', 2, 'Assets/Inventory', TRUE, TRUE, 22000.00, 'DEBIT', 'flyway', 'flyway'),
('1500', 'Fixed Assets', 'Fixed Assets', 'Ø§Ù„Ø£ØµÙˆÙ„ Ø§Ù„Ø«Ø§Ø¨ØªØ©', 'ASSET', 2, 'Assets/Fixed Assets', TRUE, TRUE, 85000.00, 'DEBIT', 'flyway', 'flyway'),
('2000', 'Liabilities', 'Liabilities', 'Ø§Ù„Ø§Ù„ØªØ²Ø§Ù…Ø§Øª', 'LIABILITY', 1, 'Liabilities', TRUE, FALSE, 0, 'CREDIT', 'flyway', 'flyway'),
('2100', 'Accounts Payable', 'Accounts Payable', 'Ø§Ù„Ø°Ù…Ù… Ø§Ù„Ø¯Ø§Ø¦Ù†Ø©', 'LIABILITY', 2, 'Liabilities/Accounts Payable', TRUE, TRUE, 14000.00, 'CREDIT', 'flyway', 'flyway'),
('2200', 'Accrued Expenses', 'Accrued Expenses', 'Ø§Ù„Ù…ØµØ±ÙˆÙØ§Øª Ø§Ù„Ù…Ø³ØªØ­Ù‚Ø©', 'LIABILITY', 2, 'Liabilities/Accrued Expenses', TRUE, TRUE, 6000.00, 'CREDIT', 'flyway', 'flyway'),
('3000', 'Equity', 'Equity', 'Ø­Ù‚ÙˆÙ‚ Ø§Ù„Ù…Ù„ÙƒÙŠØ©', 'EQUITY', 1, 'Equity', TRUE, FALSE, 0, 'CREDIT', 'flyway', 'flyway'),
('3100', 'Owner Capital', 'Owner Capital', 'Ø±Ø£Ø³ Ø§Ù„Ù…Ø§Ù„', 'EQUITY', 2, 'Equity/Owner Capital', TRUE, TRUE, 90000.00, 'CREDIT', 'flyway', 'flyway'),
('3200', 'Retained Earnings', 'Retained Earnings', 'Ø§Ù„Ø£Ø±Ø¨Ø§Ø­ Ø§Ù„Ù…Ø­ØªØ¬Ø²Ø©', 'EQUITY', 2, 'Equity/Retained Earnings', TRUE, TRUE, 8000.00, 'CREDIT', 'flyway', 'flyway'),
('4000', 'Revenue', 'Revenue', 'Ø§Ù„Ø¥ÙŠØ±Ø§Ø¯Ø§Øª', 'INCOME', 1, 'Revenue', TRUE, FALSE, 0, 'CREDIT', 'flyway', 'flyway'),
('4100', 'Sales Revenue', 'Sales Revenue', 'Ø¥ÙŠØ±Ø§Ø¯Ø§Øª Ø§Ù„Ù…Ø¨ÙŠØ¹Ø§Øª', 'INCOME', 2, 'Revenue/Sales Revenue', TRUE, TRUE, 0, 'CREDIT', 'flyway', 'flyway'),
('4200', 'Service Revenue', 'Service Revenue', 'Ø¥ÙŠØ±Ø§Ø¯Ø§Øª Ø§Ù„Ø®Ø¯Ù…Ø§Øª', 'INCOME', 2, 'Revenue/Service Revenue', TRUE, TRUE, 0, 'CREDIT', 'flyway', 'flyway'),
('5000', 'Expenses', 'Expenses', 'Ø§Ù„Ù…ØµØ±ÙˆÙØ§Øª', 'EXPENSE', 1, 'Expenses', TRUE, FALSE, 0, 'DEBIT', 'flyway', 'flyway'),
('5100', 'Salary Expense', 'Salary Expense', 'Ù…ØµØ±ÙˆÙ Ø§Ù„Ø±ÙˆØ§ØªØ¨', 'EXPENSE', 2, 'Expenses/Salary Expense', TRUE, TRUE, 0, 'DEBIT', 'flyway', 'flyway'),
('5200', 'Rent Expense', 'Rent Expense', 'Ù…ØµØ±ÙˆÙ Ø§Ù„Ø¥ÙŠØ¬Ø§Ø±', 'EXPENSE', 2, 'Expenses/Rent Expense', TRUE, TRUE, 0, 'DEBIT', 'flyway', 'flyway'),
('5300', 'Utilities Expense', 'Utilities Expense', 'Ù…ØµØ±ÙˆÙ Ø§Ù„Ù…Ø±Ø§ÙÙ‚', 'EXPENSE', 2, 'Expenses/Utilities Expense', TRUE, TRUE, 0, 'DEBIT', 'flyway', 'flyway'),
('5400', 'Office Supplies Expense', 'Office Supplies Expense', 'Ù…ØµØ±ÙˆÙ Ø§Ù„Ù‚Ø±Ø·Ø§Ø³ÙŠØ©', 'EXPENSE', 2, 'Expenses/Office Supplies Expense', TRUE, TRUE, 0, 'DEBIT', 'flyway', 'flyway')
ON CONFLICT (code) DO UPDATE
SET name = EXCLUDED.name,
    name_en = EXCLUDED.name_en,
    name_ar = EXCLUDED.name_ar,
    account_type = EXCLUDED.account_type,
    is_active = EXCLUDED.is_active,
    is_postable = EXCLUDED.is_postable,
    updated_by = 'flyway';

UPDATE accounts child
SET parent_id = parent.id,
    level = CASE WHEN parent.parent_id IS NULL THEN 2 ELSE 3 END
FROM accounts parent
WHERE child.code IN ('1100', '1200', '1300', '1500', '2100', '2200', '3100', '3200', '4100', '4200', '5100', '5200', '5300', '5400')
  AND (
      (child.code IN ('1100', '1200', '1300', '1500') AND parent.code = '1000')
      OR (child.code IN ('2100', '2200') AND parent.code = '2000')
      OR (child.code IN ('3100', '3200') AND parent.code = '3000')
      OR (child.code IN ('4100', '4200') AND parent.code = '4000')
      OR (child.code IN ('5100', '5200', '5300', '5400') AND parent.code = '5000')
  );

UPDATE accounts child
SET parent_id = parent.id,
    level = 3
FROM accounts parent
WHERE child.code IN ('1110', '1120', '1130')
  AND parent.code = '1100';

-- Bank accounts
UPDATE bank_accounts b
SET bank_name = 'Emirates NBD',
    account_number = 'ENBD-001-USD',
    iban = 'AE070331234567890123456',
    currency = 'USD',
    opening_balance = 35000.00,
    current_balance = 37150.00,
    is_active = TRUE,
    updated_by = 'flyway'
FROM accounts a
WHERE a.code = '1120'
  AND b.linked_account_id = a.id;

INSERT INTO bank_accounts (bank_name, account_number, iban, currency, opening_balance, current_balance, linked_account_id, is_active, created_by, updated_by)
SELECT 'Emirates NBD', 'ENBD-001-USD', 'AE070331234567890123456', 'USD', 35000.00, 37150.00, a.id, TRUE, 'flyway', 'flyway'
FROM accounts a
WHERE a.code = '1120'
  AND NOT EXISTS (
      SELECT 1
      FROM bank_accounts b
      WHERE b.linked_account_id = a.id
         OR b.account_number = 'ENBD-001-USD'
  );

UPDATE bank_accounts b
SET bank_name = 'Abu Dhabi Islamic Bank',
    account_number = 'ADIB-002-AED',
    iban = 'AE120331234567890123457',
    currency = 'AED',
    opening_balance = 12000.00,
    current_balance = 14520.00,
    is_active = TRUE,
    updated_by = 'flyway'
FROM accounts a
WHERE a.code = '1130'
  AND b.linked_account_id = a.id;

INSERT INTO bank_accounts (bank_name, account_number, iban, currency, opening_balance, current_balance, linked_account_id, is_active, created_by, updated_by)
SELECT 'Abu Dhabi Islamic Bank', 'ADIB-002-AED', 'AE120331234567890123457', 'AED', 12000.00, 14520.00, a.id, TRUE, 'flyway', 'flyway'
FROM accounts a
WHERE a.code = '1130'
  AND NOT EXISTS (
      SELECT 1
      FROM bank_accounts b
      WHERE b.linked_account_id = a.id
         OR b.account_number = 'ADIB-002-AED'
  );

-- Posted journals for meaningful P&L and balance sheet
WITH header AS (
    INSERT INTO journal_entries (
        entry_date, reference_number, description, status, total_debit, total_credit, posted_at, posted_by,
        source_module, source_record_id, external_reference, currency_code, entry_type, created_by, updated_by
    )
    VALUES
    (CURRENT_DATE - 25, 'JE-DEMO-0001', 'Demo sale invoice posting', 'POSTED', 8500.00, 8500.00, NOW(), 'seed', 'DEMO', 1, 'INV-DEMO-001', 'USD', 'MANUAL', 'flyway', 'flyway'),
    (CURRENT_DATE - 18, 'JE-DEMO-0002', 'Demo salary payment', 'POSTED', 3200.00, 3200.00, NOW(), 'seed', 'DEMO', 2, 'PAY-DEMO-001', 'USD', 'MANUAL', 'flyway', 'flyway'),
    (CURRENT_DATE - 10, 'JE-DEMO-0003', 'Demo rent and utilities', 'POSTED', 2100.00, 2100.00, NOW(), 'seed', 'DEMO', 3, 'PAY-DEMO-002', 'USD', 'MANUAL', 'flyway', 'flyway'),
    (CURRENT_DATE - 5, 'JE-DEMO-0004', 'Demo service receipt', 'POSTED', 4400.00, 4400.00, NOW(), 'seed', 'DEMO', 4, 'REC-DEMO-001', 'USD', 'MANUAL', 'flyway', 'flyway')
    ON CONFLICT (reference_number) DO NOTHING
    RETURNING id, reference_number
)
SELECT 1;

INSERT INTO journal_entry_lines (journal_entry_id, line_number, account_id, debit, credit, description, created_by, updated_by)
SELECT je.id, 1, ar.id, 8500.00, 0.00, 'Accounts receivable debit', 'flyway', 'flyway'
FROM journal_entries je
JOIN accounts ar ON ar.code = '1200'
WHERE je.reference_number = 'JE-DEMO-0001'
  AND NOT EXISTS (SELECT 1 FROM journal_entry_lines l WHERE l.journal_entry_id = je.id AND l.line_number = 1);

INSERT INTO journal_entry_lines (journal_entry_id, line_number, account_id, debit, credit, description, created_by, updated_by)
SELECT je.id, 2, rev.id, 0.00, 8500.00, 'Sales revenue credit', 'flyway', 'flyway'
FROM journal_entries je
JOIN accounts rev ON rev.code = '4100'
WHERE je.reference_number = 'JE-DEMO-0001'
  AND NOT EXISTS (SELECT 1 FROM journal_entry_lines l WHERE l.journal_entry_id = je.id AND l.line_number = 2);

INSERT INTO journal_entry_lines (journal_entry_id, line_number, account_id, debit, credit, description, created_by, updated_by)
SELECT je.id, 1, sal.id, 3200.00, 0.00, 'Salary expense debit', 'flyway', 'flyway'
FROM journal_entries je
JOIN accounts sal ON sal.code = '5100'
WHERE je.reference_number = 'JE-DEMO-0002'
  AND NOT EXISTS (SELECT 1 FROM journal_entry_lines l WHERE l.journal_entry_id = je.id AND l.line_number = 1);

INSERT INTO journal_entry_lines (journal_entry_id, line_number, account_id, debit, credit, description, created_by, updated_by)
SELECT je.id, 2, bank.id, 0.00, 3200.00, 'Bank credit', 'flyway', 'flyway'
FROM journal_entries je
JOIN accounts bank ON bank.code = '1120'
WHERE je.reference_number = 'JE-DEMO-0002'
  AND NOT EXISTS (SELECT 1 FROM journal_entry_lines l WHERE l.journal_entry_id = je.id AND l.line_number = 2);

INSERT INTO journal_entry_lines (journal_entry_id, line_number, account_id, debit, credit, description, created_by, updated_by)
SELECT je.id, 1, rent.id, 1700.00, 0.00, 'Rent expense debit', 'flyway', 'flyway'
FROM journal_entries je
JOIN accounts rent ON rent.code = '5200'
WHERE je.reference_number = 'JE-DEMO-0003'
  AND NOT EXISTS (SELECT 1 FROM journal_entry_lines l WHERE l.journal_entry_id = je.id AND l.line_number = 1);

INSERT INTO journal_entry_lines (journal_entry_id, line_number, account_id, debit, credit, description, created_by, updated_by)
SELECT je.id, 2, util.id, 400.00, 0.00, 'Utilities expense debit', 'flyway', 'flyway'
FROM journal_entries je
JOIN accounts util ON util.code = '5300'
WHERE je.reference_number = 'JE-DEMO-0003'
  AND NOT EXISTS (SELECT 1 FROM journal_entry_lines l WHERE l.journal_entry_id = je.id AND l.line_number = 2);

INSERT INTO journal_entry_lines (journal_entry_id, line_number, account_id, debit, credit, description, created_by, updated_by)
SELECT je.id, 3, bank.id, 0.00, 2100.00, 'Bank credit', 'flyway', 'flyway'
FROM journal_entries je
JOIN accounts bank ON bank.code = '1120'
WHERE je.reference_number = 'JE-DEMO-0003'
  AND NOT EXISTS (SELECT 1 FROM journal_entry_lines l WHERE l.journal_entry_id = je.id AND l.line_number = 3);

INSERT INTO journal_entry_lines (journal_entry_id, line_number, account_id, debit, credit, description, created_by, updated_by)
SELECT je.id, 1, bank.id, 4400.00, 0.00, 'Bank debit', 'flyway', 'flyway'
FROM journal_entries je
JOIN accounts bank ON bank.code = '1120'
WHERE je.reference_number = 'JE-DEMO-0004'
  AND NOT EXISTS (SELECT 1 FROM journal_entry_lines l WHERE l.journal_entry_id = je.id AND l.line_number = 1);

INSERT INTO journal_entry_lines (journal_entry_id, line_number, account_id, debit, credit, description, created_by, updated_by)
SELECT je.id, 2, srv.id, 0.00, 4400.00, 'Service revenue credit', 'flyway', 'flyway'
FROM journal_entries je
JOIN accounts srv ON srv.code = '4200'
WHERE je.reference_number = 'JE-DEMO-0004'
  AND NOT EXISTS (SELECT 1 FROM journal_entry_lines l WHERE l.journal_entry_id = je.id AND l.line_number = 2);

-- Payment vouchers (one posted, one approved, one draft)
INSERT INTO payment_vouchers (
    voucher_date, reference, description, amount, status, payment_method, party_name, linked_document_reference,
    currency_code, voucher_type, account_id, cash_account_id, expense_account_id, journal_entry_id,
    approved_at, approved_by, posted_at, posted_by, created_by, updated_by
)
SELECT CURRENT_DATE - 18, 'PV-DEMO-0001', 'Salary payment for March payroll', 3200.00, 'POSTED', 'BANK', 'Team Payroll', 'PAYROLL-MAR',
       'USD', 'STANDARD', expense.id, cash.id, expense.id, je.id, NOW() - INTERVAL '19 days', 'seed', NOW() - INTERVAL '18 days', 'seed', 'flyway', 'flyway'
FROM accounts cash
JOIN accounts expense ON expense.code = '5100'
JOIN journal_entries je ON je.reference_number = 'JE-DEMO-0002'
WHERE cash.code = '1120'
  AND NOT EXISTS (SELECT 1 FROM payment_vouchers pv WHERE pv.reference = 'PV-DEMO-0001');

INSERT INTO payment_vouchers (
    voucher_date, reference, description, amount, status, payment_method, party_name, linked_document_reference,
    currency_code, voucher_type, account_id, cash_account_id, expense_account_id, journal_entry_id,
    approved_at, approved_by, created_by, updated_by
)
SELECT CURRENT_DATE - 2, 'PV-DEMO-0002', 'Office supplies pending payment', 650.00, 'APPROVED', 'BANK', 'Office Mart LLC', 'BILL-2026-044',
       'USD', 'BILL_PAYMENT', expense.id, cash.id, expense.id, je.id, NOW() - INTERVAL '1 day', 'seed', 'flyway', 'flyway'
FROM accounts cash
JOIN accounts expense ON expense.code = '5400'
JOIN journal_entries je ON je.reference_number = 'JE-DEMO-0003'
WHERE cash.code = '1120'
  AND NOT EXISTS (SELECT 1 FROM payment_vouchers pv WHERE pv.reference = 'PV-DEMO-0002');

INSERT INTO payment_vouchers (
    voucher_date, reference, description, amount, status, payment_method, party_name, linked_document_reference,
    currency_code, voucher_type, account_id, cash_account_id, expense_account_id, journal_entry_id, created_by, updated_by
)
SELECT CURRENT_DATE, 'PV-DEMO-0003', 'Accrued expense draft settlement', 900.00, 'DRAFT', 'CASH', 'Facilities Vendor', 'ACC-EXP-11',
       'USD', 'STANDARD', expense.id, cash.id, expense.id, je.id, 'flyway', 'flyway'
FROM accounts cash
JOIN accounts expense ON expense.code = '2200'
JOIN journal_entries je ON je.reference_number = 'JE-DEMO-0001'
WHERE cash.code = '1110'
  AND NOT EXISTS (SELECT 1 FROM payment_vouchers pv WHERE pv.reference = 'PV-DEMO-0003');

-- Receipt vouchers (one posted, one approved, one draft)
INSERT INTO receipt_vouchers (
    voucher_date, reference, description, amount, status, payment_method, party_name, invoice_reference,
    currency_code, voucher_type, account_id, cash_account_id, revenue_account_id, journal_entry_id,
    approved_at, approved_by, posted_at, posted_by, created_by, updated_by
)
SELECT CURRENT_DATE - 5, 'RV-DEMO-0001', 'Service revenue receipt', 4400.00, 'POSTED', 'BANK', 'Acme Services Client', 'INV-DEMO-020',
       'USD', 'INVOICE_COLLECTION', revenue.id, cash.id, revenue.id, je.id, NOW() - INTERVAL '6 days', 'seed', NOW() - INTERVAL '5 days', 'seed', 'flyway', 'flyway'
FROM accounts cash
JOIN accounts revenue ON revenue.code = '4200'
JOIN journal_entries je ON je.reference_number = 'JE-DEMO-0004'
WHERE cash.code = '1120'
  AND NOT EXISTS (SELECT 1 FROM receipt_vouchers rv WHERE rv.reference = 'RV-DEMO-0001');

INSERT INTO receipt_vouchers (
    voucher_date, reference, description, amount, status, payment_method, party_name, invoice_reference,
    currency_code, voucher_type, account_id, cash_account_id, revenue_account_id, journal_entry_id, approved_at, approved_by, created_by, updated_by
)
SELECT CURRENT_DATE - 1, 'RV-DEMO-0002', 'Advance customer collection', 1850.00, 'APPROVED', 'BANK', 'Blue Ocean Trading', 'ADV-CUST-04',
       'USD', 'ADVANCE', revenue.id, cash.id, revenue.id, je.id, NOW() - INTERVAL '1 day', 'seed', 'flyway', 'flyway'
FROM accounts cash
JOIN accounts revenue ON revenue.code = '4100'
JOIN journal_entries je ON je.reference_number = 'JE-DEMO-0001'
WHERE cash.code = '1120'
  AND NOT EXISTS (SELECT 1 FROM receipt_vouchers rv WHERE rv.reference = 'RV-DEMO-0002');

INSERT INTO receipt_vouchers (
    voucher_date, reference, description, amount, status, payment_method, party_name, invoice_reference,
    currency_code, voucher_type, account_id, cash_account_id, revenue_account_id, journal_entry_id, created_by, updated_by
)
SELECT CURRENT_DATE, 'RV-DEMO-0003', 'Cash collection draft', 700.00, 'DRAFT', 'CASH', 'Walk-in Customer', 'POS-044',
       'USD', 'STANDARD', revenue.id, cash.id, revenue.id, je.id, 'flyway', 'flyway'
FROM accounts cash
JOIN accounts revenue ON revenue.code = '4100'
JOIN journal_entries je ON je.reference_number = 'JE-DEMO-0003'
WHERE cash.code = '1110'
  AND NOT EXISTS (SELECT 1 FROM receipt_vouchers rv WHERE rv.reference = 'RV-DEMO-0003');

-- Transactions history
INSERT INTO transactions (
    transaction_date, reference, description, transaction_type, status, amount, debit_account_id, credit_account_id,
    related_document_reference, created_by, updated_by
)
SELECT CURRENT_DATE - 5, 'TX-DEMO-0001', 'Bank receipt transaction', 'SALE', 'COMPLETED', 4400.00, debit_acc.id, credit_acc.id, 'RV-DEMO-0001', 'flyway', 'flyway'
FROM accounts debit_acc
JOIN accounts credit_acc ON credit_acc.code = '4200'
WHERE debit_acc.code = '1120'
  AND NOT EXISTS (SELECT 1 FROM transactions t WHERE t.reference = 'TX-DEMO-0001');

INSERT INTO transactions (
    transaction_date, reference, description, transaction_type, status, amount, debit_account_id, credit_account_id,
    related_document_reference, created_by, updated_by
)
SELECT CURRENT_DATE - 18, 'TX-DEMO-0002', 'Salary payment transaction', 'PURCHASE', 'COMPLETED', 3200.00, debit_acc.id, credit_acc.id, 'PV-DEMO-0001', 'flyway', 'flyway'
FROM accounts debit_acc
JOIN accounts credit_acc ON credit_acc.code = '1120'
WHERE debit_acc.code = '5100'
  AND NOT EXISTS (SELECT 1 FROM transactions t WHERE t.reference = 'TX-DEMO-0002');

-- Reconciliation sample with matched + unmatched lines
INSERT INTO reconciliations (
    bank_account_id, statement_start_date, statement_end_date, opening_balance, closing_balance,
    system_ending_balance, difference, status, created_by, updated_by
)
SELECT b.id, CURRENT_DATE - 30, CURRENT_DATE, 35000.00, 37150.00, 37150.00, 0.00, 'OPEN', 'flyway', 'flyway'
FROM bank_accounts b
WHERE b.account_number = 'ENBD-001-USD'
  AND NOT EXISTS (
      SELECT 1 FROM reconciliations r
      WHERE r.bank_account_id = b.id
        AND r.statement_start_date = CURRENT_DATE - 30
        AND r.statement_end_date = CURRENT_DATE
  );

WITH rec AS (
    SELECT id
    FROM reconciliations
    WHERE statement_start_date = CURRENT_DATE - 30
      AND statement_end_date = CURRENT_DATE
    ORDER BY id DESC
    LIMIT 1
)
INSERT INTO reconciliation_lines (
    reconciliation_id, transaction_date, description, amount, transaction_type, status, source_reference,
    journal_entry_line_id, matched_line_id, matched_amount, created_by, updated_by
)
SELECT rec.id, CURRENT_DATE - 5, 'Bank statement service receipt', 4400.00, 'BANK_STATEMENT', 'MATCHED', 'STMT-001',
       NULL, NULL, 4400.00, 'flyway', 'flyway'
FROM rec
WHERE NOT EXISTS (
    SELECT 1 FROM reconciliation_lines line
    WHERE line.reconciliation_id = rec.id
      AND line.source_reference = 'STMT-001'
);

WITH rec AS (
    SELECT id
    FROM reconciliations
    WHERE statement_start_date = CURRENT_DATE - 30
      AND statement_end_date = CURRENT_DATE
    ORDER BY id DESC
    LIMIT 1
)
INSERT INTO reconciliation_lines (
    reconciliation_id, transaction_date, description, amount, transaction_type, status, source_reference,
    journal_entry_line_id, matched_line_id, matched_amount, created_by, updated_by
)
SELECT rec.id, CURRENT_DATE - 4, 'Monthly bank fee', 75.00, 'BANK_STATEMENT', 'UNMATCHED', 'STMT-002',
       NULL, NULL, NULL, 'flyway', 'flyway'
FROM rec
WHERE NOT EXISTS (
    SELECT 1 FROM reconciliation_lines line
    WHERE line.reconciliation_id = rec.id
      AND line.source_reference = 'STMT-002'
);

WITH rec AS (
    SELECT id
    FROM reconciliations
    WHERE statement_start_date = CURRENT_DATE - 30
      AND statement_end_date = CURRENT_DATE
    ORDER BY id DESC
    LIMIT 1
),
je_line AS (
    SELECT line.id
    FROM journal_entry_lines line
    JOIN journal_entries je ON je.id = line.journal_entry_id
    JOIN accounts acc ON acc.id = line.account_id
    WHERE je.reference_number = 'JE-DEMO-0004'
      AND acc.code = '1120'
    LIMIT 1
)
INSERT INTO reconciliation_lines (
    reconciliation_id, transaction_date, description, amount, transaction_type, status, source_reference,
    journal_entry_line_id, matched_line_id, matched_amount, created_by, updated_by
)
SELECT rec.id, CURRENT_DATE - 5, 'ERP receipt posting', 4400.00, 'SYSTEM_TRANSACTION', 'MATCHED', 'JE-DEMO-0004',
       je_line.id, NULL, 4400.00, 'flyway', 'flyway'
FROM rec, je_line
WHERE NOT EXISTS (
    SELECT 1 FROM reconciliation_lines line
    WHERE line.reconciliation_id = rec.id
      AND line.source_reference = 'JE-DEMO-0004'
);

WITH rec AS (
    SELECT id
    FROM reconciliations
    WHERE statement_start_date = CURRENT_DATE - 30
      AND statement_end_date = CURRENT_DATE
    ORDER BY id DESC
    LIMIT 1
),
je_line AS (
    SELECT line.id
    FROM journal_entry_lines line
    JOIN journal_entries je ON je.id = line.journal_entry_id
    JOIN accounts acc ON acc.id = line.account_id
    WHERE je.reference_number = 'JE-DEMO-0002'
      AND acc.code = '1120'
    LIMIT 1
)
INSERT INTO reconciliation_lines (
    reconciliation_id, transaction_date, description, amount, transaction_type, status, source_reference,
    journal_entry_line_id, matched_line_id, matched_amount, created_by, updated_by
)
SELECT rec.id, CURRENT_DATE - 18, 'ERP salary disbursement', 3200.00, 'SYSTEM_TRANSACTION', 'UNMATCHED', 'JE-DEMO-0002',
       je_line.id, NULL, NULL, 'flyway', 'flyway'
FROM rec, je_line
WHERE NOT EXISTS (
    SELECT 1 FROM reconciliation_lines line
    WHERE line.reconciliation_id = rec.id
      AND line.source_reference = 'JE-DEMO-0002'
);


-- ---------- source: V18__auth_password_reset_otps.sql ----------
SET search_path TO erp_system, public;

CREATE TABLE IF NOT EXISTS password_reset_otps (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(190) NOT NULL,
    otp_code VARCHAR(12) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_password_reset_otps_email_created
    ON password_reset_otps (email, created_at DESC);


-- ---------- source: V19__rename_user_role_to_accountant.sql ----------
SET search_path TO erp_system, public;

UPDATE users
SET role = 'ACCOUNTANT'
WHERE role = 'USER';

ALTER TABLE users
    DROP CONSTRAINT IF EXISTS chk_users_role;

ALTER TABLE users
    ADD CONSTRAINT chk_users_role CHECK (role IN ('ADMIN', 'ACCOUNTANT'));

UPDATE erp_system.ui_menu_items
SET roles_csv = REPLACE(roles_csv, 'USER', 'ACCOUNTANT')
WHERE roles_csv LIKE '%USER%';


-- ---------- source: V20__dynamic_lookup_completion.sql ----------
SET search_path TO erp_system, public;

INSERT INTO lookup_values (type_code, code, sort_order, is_active, created_by, updated_by)
VALUES
('statuses', 'ACTIVE', 1, TRUE, 'flyway', 'flyway'),
('statuses', 'INACTIVE', 2, TRUE, 'flyway', 'flyway'),
('accounting-methods', 'ACCRUAL', 1, TRUE, 'flyway', 'flyway'),
('accounting-methods', 'CASH', 2, TRUE, 'flyway', 'flyway'),
('transfer-statuses', 'DRAFT', 1, TRUE, 'flyway', 'flyway'),
('transfer-statuses', 'POSTED', 2, TRUE, 'flyway', 'flyway'),
('transfer-statuses', 'CANCELLED', 3, TRUE, 'flyway', 'flyway'),
('transaction-types', 'SALE', 1, TRUE, 'flyway', 'flyway'),
('transaction-types', 'PURCHASE', 2, TRUE, 'flyway', 'flyway'),
('transaction-types', 'REFUND', 3, TRUE, 'flyway', 'flyway'),
('transaction-types', 'ADJUSTMENT', 4, TRUE, 'flyway', 'flyway'),
('transaction-statuses', 'DRAFT', 1, TRUE, 'flyway', 'flyway'),
('transaction-statuses', 'POSTED', 2, TRUE, 'flyway', 'flyway'),
('transaction-statuses', 'CANCELLED', 3, TRUE, 'flyway', 'flyway'),
('bill-statuses', 'DRAFT', 1, TRUE, 'flyway', 'flyway'),
('bill-statuses', 'APPROVED', 2, TRUE, 'flyway', 'flyway'),
('bill-statuses', 'POSTED', 3, TRUE, 'flyway', 'flyway'),
('bill-statuses', 'PARTIALLY_PAID', 4, TRUE, 'flyway', 'flyway'),
('bill-statuses', 'PAID', 5, TRUE, 'flyway', 'flyway'),
('bill-statuses', 'CANCELLED', 6, TRUE, 'flyway', 'flyway'),
('budget-statuses', 'DRAFT', 1, TRUE, 'flyway', 'flyway'),
('budget-statuses', 'APPROVED', 2, TRUE, 'flyway', 'flyway'),
('budget-statuses', 'ACTIVE', 3, TRUE, 'flyway', 'flyway'),
('budget-statuses', 'CLOSED', 4, TRUE, 'flyway', 'flyway'),
('check-types', 'ISSUED', 1, TRUE, 'flyway', 'flyway'),
('check-types', 'RECEIVED', 2, TRUE, 'flyway', 'flyway'),
('check-statuses', 'PENDING', 1, TRUE, 'flyway', 'flyway'),
('check-statuses', 'DEPOSITED', 2, TRUE, 'flyway', 'flyway'),
('check-statuses', 'CLEARED', 3, TRUE, 'flyway', 'flyway'),
('check-statuses', 'BOUNCED', 4, TRUE, 'flyway', 'flyway'),
('check-statuses', 'CANCELLED', 5, TRUE, 'flyway', 'flyway')
ON CONFLICT (type_code, code) DO NOTHING;


-- ---------- source: V21__transaction_status_alignment.sql ----------
SET search_path TO erp_system, public;

ALTER TABLE transactions
    DROP CONSTRAINT IF EXISTS transactions_status_check;

ALTER TABLE transactions
    DROP CONSTRAINT IF EXISTS chk_transactions_status;

UPDATE transactions
SET status = CASE status
    WHEN 'PENDING' THEN 'DRAFT'
    WHEN 'COMPLETED' THEN 'POSTED'
    ELSE status
END
WHERE status IN ('PENDING', 'COMPLETED');

ALTER TABLE transactions
    ALTER COLUMN status SET DEFAULT 'DRAFT';

ALTER TABLE transactions
    ADD CONSTRAINT chk_transactions_status
        CHECK (status IN ('DRAFT', 'POSTED', 'CANCELLED'));


-- ---------- source: V22__align_ui_menu_with_erp_routes.sql ----------
SET search_path TO erp_system, public;

DELETE FROM erp_system.ui_menu_items
WHERE id IN ('dashboard', 'accounting', 'authentication', 'utilities', 'other');

INSERT INTO erp_system.ui_menu_items (id, parent_id, sort_order, item_type, title_key, icon, url, is_external, target_blank, roles_csv, item_classes, breadcrumbs_flag)
VALUES
    ('hesabaty', NULL, 0, 'group', 'NAV.HESABATY', 'apartment', NULL, FALSE, FALSE, 'ADMIN,ACCOUNTANT', NULL, NULL),
    ('dashboard', 'hesabaty', 0, 'item', 'NAV.DASHBOARD', 'space_dashboard', '/dashboard', FALSE, FALSE, 'ADMIN,ACCOUNTANT', 'nav-item', FALSE),
    ('accounts', 'hesabaty', 1, 'item', 'NAV.CHART_OF_ACCOUNTS', 'account_tree', '/accounts', FALSE, FALSE, 'ADMIN,ACCOUNTANT', 'nav-item', FALSE),
    ('journal-entries', 'hesabaty', 2, 'item', 'NAV.JOURNAL_ENTRIES', 'menu_book', '/journal-entries', FALSE, FALSE, 'ADMIN,ACCOUNTANT', 'nav-item', FALSE),
    ('payment-vouchers', 'hesabaty', 3, 'item', 'NAV.PAYMENT_VOUCHERS', 'payments', '/payment-vouchers', FALSE, FALSE, 'ADMIN,ACCOUNTANT', 'nav-item', FALSE),
    ('receipt-vouchers', 'hesabaty', 4, 'item', 'NAV.RECEIPT_VOUCHERS', 'receipt_long', '/receipt-vouchers', FALSE, FALSE, 'ADMIN,ACCOUNTANT', 'nav-item', FALSE),
    ('transfers', 'hesabaty', 5, 'item', 'NAV.TRANSFERS', 'swap_horiz', '/transfers', FALSE, FALSE, 'ADMIN,ACCOUNTANT', 'nav-item', FALSE),
    ('transactions', 'hesabaty', 6, 'item', 'NAV.TRANSACTIONS', 'sync_alt', '/transactions', FALSE, FALSE, 'ADMIN,ACCOUNTANT', 'nav-item', FALSE),
    ('invoices', 'hesabaty', 7, 'item', 'NAV.INVOICES', 'request_quote', '/invoices', FALSE, FALSE, 'ADMIN,ACCOUNTANT', 'nav-item', FALSE),
    ('checks', 'hesabaty', 8, 'item', 'NAV.CHECKS', 'rule', '/checks', FALSE, FALSE, 'ADMIN,ACCOUNTANT', 'nav-item', FALSE),
    ('bank-accounts', 'hesabaty', 9, 'item', 'NAV.BANK_ACCOUNTS', 'account_balance', '/bank-accounts', FALSE, FALSE, 'ADMIN,ACCOUNTANT', 'nav-item', FALSE),
    ('ledger', 'hesabaty', 10, 'item', 'NAV.LEDGER', 'library_books', '/ledger', FALSE, FALSE, 'ADMIN,ACCOUNTANT', 'nav-item', FALSE),
    ('reconciliation', 'hesabaty', 11, 'item', 'NAV.RECONCILIATION', 'fact_check', '/reconciliation', FALSE, FALSE, 'ADMIN,ACCOUNTANT', 'nav-item', FALSE),
    ('reports', 'hesabaty', 12, 'item', 'NAV.REPORTS', 'insert_chart', '/reports', FALSE, FALSE, 'ADMIN,ACCOUNTANT', 'nav-item', FALSE),
    ('settings', 'hesabaty', 13, 'item', 'NAV.SETTINGS', 'tune', '/settings', FALSE, FALSE, 'ADMIN,ACCOUNTANT', 'nav-item', FALSE),
    ('accountants', 'hesabaty', 14, 'item', 'NAV.ACCOUNTANTS_PORTAL', 'groups', '/accountants', FALSE, FALSE, 'ADMIN', 'nav-item', FALSE)
ON CONFLICT (id) DO UPDATE
SET parent_id = EXCLUDED.parent_id,
    sort_order = EXCLUDED.sort_order,
    item_type = EXCLUDED.item_type,
    title_key = EXCLUDED.title_key,
    icon = EXCLUDED.icon,
    url = EXCLUDED.url,
    is_external = EXCLUDED.is_external,
    target_blank = EXCLUDED.target_blank,
    roles_csv = EXCLUDED.roles_csv,
    item_classes = EXCLUDED.item_classes,
    breadcrumbs_flag = EXCLUDED.breadcrumbs_flag;


-- ---------- source: V23__normalize_demo_opening_balances.sql ----------
SET search_path TO erp_system, public;

-- Align demo opening balances with the intended enterprise sample chart of accounts.
UPDATE accounts
SET opening_balance = CASE code
        WHEN '1110' THEN 2500.00
        WHEN '1120' THEN 35000.00
        WHEN '1130' THEN 12000.00
        WHEN '1200' THEN 18000.00
        WHEN '1300' THEN 22000.00
        WHEN '1500' THEN 85000.00
        WHEN '2100' THEN 14000.00
        WHEN '2200' THEN 6000.00
        WHEN '3100' THEN 90000.00
        WHEN '3200' THEN 64500.00
        ELSE opening_balance
    END,
    opening_balance_side = CASE code
        WHEN '1110' THEN 'DEBIT'
        WHEN '1120' THEN 'DEBIT'
        WHEN '1130' THEN 'DEBIT'
        WHEN '1200' THEN 'DEBIT'
        WHEN '1300' THEN 'DEBIT'
        WHEN '1500' THEN 'DEBIT'
        WHEN '2100' THEN 'CREDIT'
        WHEN '2200' THEN 'CREDIT'
        WHEN '3100' THEN 'CREDIT'
        WHEN '3200' THEN 'CREDIT'
        ELSE opening_balance_side
    END,
    updated_by = 'flyway'
WHERE code IN ('1110', '1120', '1130', '1200', '1300', '1500', '2100', '2200', '3100', '3200');

-- Stored bank balances should match the ledger-based current balance used by the services.
UPDATE bank_accounts b
SET current_balance = (
        COALESCE(b.opening_balance, 0)
        + COALESCE((
            SELECT SUM(line.debit - line.credit)
            FROM journal_entry_lines line
            JOIN journal_entries entry ON entry.id = line.journal_entry_id
            WHERE line.account_id = b.linked_account_id
              AND entry.status IN ('POSTED', 'REVERSED')
        ), 0)
    ),
    updated_by = 'flyway';


-- ---------- source: V24__access_control_and_lookup_admin.sql ----------
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
    ('account-types', 'Account Types', 'Ø£Ù†ÙˆØ§Ø¹ Ø§Ù„Ø­Ø³Ø§Ø¨Ø§Øª', 1, TRUE, 'flyway', 'flyway'),
    ('accounting-methods', 'Accounting Methods', 'Ø·Ø±Ù‚ Ø§Ù„Ù…Ø­Ø§Ø³Ø¨Ø©', 2, TRUE, 'flyway', 'flyway'),
    ('bill-statuses', 'Bill Statuses', 'Ø­Ø§Ù„Ø§Øª Ø§Ù„ÙÙˆØ§ØªÙŠØ±', 3, TRUE, 'flyway', 'flyway'),
    ('budget-statuses', 'Budget Statuses', 'Ø­Ø§Ù„Ø§Øª Ø§Ù„Ù…ÙˆØ§Ø²Ù†Ø§Øª', 4, TRUE, 'flyway', 'flyway'),
    ('check-statuses', 'Check Statuses', 'Ø­Ø§Ù„Ø§Øª Ø§Ù„Ø´ÙŠÙƒØ§Øª', 5, TRUE, 'flyway', 'flyway'),
    ('check-types', 'Check Types', 'Ø£Ù†ÙˆØ§Ø¹ Ø§Ù„Ø´ÙŠÙƒØ§Øª', 6, TRUE, 'flyway', 'flyway'),
    ('currencies', 'Currencies', 'Ø§Ù„Ø¹Ù…Ù„Ø§Øª', 7, TRUE, 'flyway', 'flyway'),
    ('entry-types', 'Journal Entry Types', 'Ø£Ù†ÙˆØ§Ø¹ Ø§Ù„Ù‚ÙŠÙˆØ¯', 8, TRUE, 'flyway', 'flyway'),
    ('journal-entry-statuses', 'Journal Entry Statuses', 'Ø­Ø§Ù„Ø§Øª Ø§Ù„Ù‚ÙŠÙˆØ¯', 9, TRUE, 'flyway', 'flyway'),
    ('payment-methods', 'Payment Methods', 'Ø·Ø±Ù‚ Ø§Ù„Ø¯ÙØ¹', 10, TRUE, 'flyway', 'flyway'),
    ('reconciliation-line-statuses', 'Reconciliation Line Statuses', 'Ø­Ø§Ù„Ø§Øª Ø³Ø·ÙˆØ± Ø§Ù„Ù…Ø·Ø§Ø¨Ù‚Ø©', 11, TRUE, 'flyway', 'flyway'),
    ('reconciliation-statuses', 'Reconciliation Statuses', 'Ø­Ø§Ù„Ø§Øª Ø§Ù„Ù…Ø·Ø§Ø¨Ù‚Ø© Ø§Ù„Ø¨Ù†ÙƒÙŠØ©', 12, TRUE, 'flyway', 'flyway'),
    ('report-periods', 'Report Periods', 'ÙØªØ±Ø§Øª Ø§Ù„ØªÙ‚Ø§Ø±ÙŠØ±', 13, TRUE, 'flyway', 'flyway'),
    ('statuses', 'Statuses', 'Ø§Ù„Ø­Ø§Ù„Ø§Øª', 14, TRUE, 'flyway', 'flyway'),
    ('transaction-statuses', 'Transaction Statuses', 'Ø­Ø§Ù„Ø§Øª Ø§Ù„Ù…Ø¹Ø§Ù…Ù„Ø§Øª', 15, TRUE, 'flyway', 'flyway'),
    ('transaction-types', 'Transaction Types', 'Ø£Ù†ÙˆØ§Ø¹ Ø§Ù„Ù…Ø¹Ø§Ù…Ù„Ø§Øª', 16, TRUE, 'flyway', 'flyway'),
    ('transfer-statuses', 'Transfer Statuses', 'Ø­Ø§Ù„Ø§Øª Ø§Ù„ØªØ­ÙˆÙŠÙ„', 17, TRUE, 'flyway', 'flyway'),
    ('voucher-statuses', 'Voucher Statuses', 'Ø­Ø§Ù„Ø§Øª Ø§Ù„Ø³Ù†Ø¯Ø§Øª', 18, TRUE, 'flyway', 'flyway'),
    ('voucher-types', 'Voucher Types', 'Ø£Ù†ÙˆØ§Ø¹ Ø§Ù„Ø³Ù†Ø¯Ø§Øª', 19, TRUE, 'flyway', 'flyway')
ON CONFLICT (code) DO UPDATE
SET name_en = EXCLUDED.name_en,
    name_ar = EXCLUDED.name_ar,
    sort_order = EXCLUDED.sort_order,
    is_active = EXCLUDED.is_active,
    updated_by = 'flyway';

INSERT INTO access_roles (code, name_en, name_ar, is_active, is_system, created_by, updated_by)
VALUES
    ('ADMIN', 'System Administrator', 'Ù…Ø¯ÙŠØ± Ø§Ù„Ù†Ø¸Ø§Ù…', TRUE, TRUE, 'flyway', 'flyway'),
    ('ACCOUNTANT_STANDARD', 'Standard Accountant', 'Ù…Ø­Ø§Ø³Ø¨ Ù‚ÙŠØ§Ø³ÙŠ', TRUE, FALSE, 'flyway', 'flyway'),
    ('TREASURY_OPERATOR', 'Treasury Operator', 'Ù…Ø³Ø¤ÙˆÙ„ Ø®Ø²ÙŠÙ†Ø©', TRUE, FALSE, 'flyway', 'flyway'),
    ('REPORT_VIEWER', 'Report Viewer', 'Ù…Ø³ØªØ¹Ø±Ø¶ ØªÙ‚Ø§Ø±ÙŠØ±', TRUE, FALSE, 'flyway', 'flyway')
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


-- ---------- source: V25__update_reconciliation_statuses.sql ----------
SET search_path TO erp_system, public;

UPDATE lookup_values 
SET code = 'IN_PROGRESS' 
WHERE type_code = 'reconciliation-statuses' AND code = 'FINALIZED';

UPDATE lookup_values 
SET code = 'COMPLETED' 
WHERE type_code = 'reconciliation-statuses' AND code = 'CANCELLED';

UPDATE reconciliations
SET status = 'IN_PROGRESS'
WHERE status = 'FINALIZED';

UPDATE reconciliations
SET status = 'COMPLETED'
WHERE status = 'CANCELLED';


-- ---------- source: V26__conditional_demo_data_guard.sql ----------
SET search_path TO erp_system, public;

-- Tag demo-seeded transactional data so production environments can purge it.
-- Chart of accounts and bank accounts are master data and remain untouched.

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = 'erp_system' AND table_name = 'journal_entries' AND column_name = 'is_demo_data') THEN
        ALTER TABLE journal_entries ADD COLUMN is_demo_data BOOLEAN NOT NULL DEFAULT FALSE;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = 'erp_system' AND table_name = 'payment_vouchers' AND column_name = 'is_demo_data') THEN
        ALTER TABLE payment_vouchers ADD COLUMN is_demo_data BOOLEAN NOT NULL DEFAULT FALSE;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = 'erp_system' AND table_name = 'receipt_vouchers' AND column_name = 'is_demo_data') THEN
        ALTER TABLE receipt_vouchers ADD COLUMN is_demo_data BOOLEAN NOT NULL DEFAULT FALSE;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = 'erp_system' AND table_name = 'transactions' AND column_name = 'is_demo_data') THEN
        ALTER TABLE transactions ADD COLUMN is_demo_data BOOLEAN NOT NULL DEFAULT FALSE;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = 'erp_system' AND table_name = 'reconciliations' AND column_name = 'is_demo_data') THEN
        ALTER TABLE reconciliations ADD COLUMN is_demo_data BOOLEAN NOT NULL DEFAULT FALSE;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = 'erp_system' AND table_name = 'reconciliation_lines' AND column_name = 'is_demo_data') THEN
        ALTER TABLE reconciliation_lines ADD COLUMN is_demo_data BOOLEAN NOT NULL DEFAULT FALSE;
    END IF;
END $$;

-- Flag existing demo records
UPDATE journal_entries SET is_demo_data = TRUE WHERE reference_number LIKE 'JE-DEMO-%';
UPDATE payment_vouchers SET is_demo_data = TRUE WHERE reference LIKE 'PV-DEMO-%';
UPDATE receipt_vouchers SET is_demo_data = TRUE WHERE reference LIKE 'RV-DEMO-%';
UPDATE transactions SET is_demo_data = TRUE WHERE reference LIKE 'TX-DEMO-%';
UPDATE reconciliations SET is_demo_data = TRUE WHERE created_by = 'flyway' AND status IN ('OPEN', 'IN_PROGRESS');
UPDATE reconciliation_lines SET is_demo_data = TRUE WHERE created_by = 'flyway';


-- ---------- source: V27__enterprise_accounting_features.sql ----------
SET search_path TO erp_system, public;

-- ============================================================
-- Exchange Rates table (multi-currency support)
-- ============================================================
CREATE TABLE IF NOT EXISTS exchange_rates (
    id              BIGSERIAL PRIMARY KEY,
    source_currency VARCHAR(3) NOT NULL,
    target_currency VARCHAR(3) NOT NULL,
    rate            NUMERIC(19, 6) NOT NULL,
    effective_date  DATE NOT NULL,
    expiry_date     DATE,
    version         BIGINT NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    UNIQUE (source_currency, target_currency, effective_date)
);

-- Seed common exchange rates
INSERT INTO exchange_rates (source_currency, target_currency, rate, effective_date, created_by, updated_by)
VALUES
('USD', 'AED', 3.6725, CURRENT_DATE - 30, 'flyway', 'flyway'),
('USD', 'SAR', 3.7500, CURRENT_DATE - 30, 'flyway', 'flyway'),
('USD', 'EUR', 0.9200, CURRENT_DATE - 30, 'flyway', 'flyway'),
('USD', 'GBP', 0.7900, CURRENT_DATE - 30, 'flyway', 'flyway')
ON CONFLICT (source_currency, target_currency, effective_date) DO NOTHING;

-- ============================================================
-- Accounting Audit Log (action trail)
-- ============================================================
CREATE TABLE IF NOT EXISTS accounting_audit_log (
    id              BIGSERIAL PRIMARY KEY,
    entity_type     VARCHAR(50) NOT NULL,
    entity_id       BIGINT NOT NULL,
    action          VARCHAR(30) NOT NULL,
    actor           VARCHAR(100) NOT NULL,
    detail          TEXT,
    performed_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_audit_log_entity ON accounting_audit_log (entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_audit_log_actor ON accounting_audit_log (actor);

-- ============================================================
-- Optimistic locking version columns
-- ============================================================
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='erp_system' AND table_name='journal_entries' AND column_name='version') THEN
        ALTER TABLE journal_entries ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='erp_system' AND table_name='reconciliations' AND column_name='version') THEN
        ALTER TABLE reconciliations ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='erp_system' AND table_name='reconciliation_lines' AND column_name='version') THEN
        ALTER TABLE reconciliation_lines ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='erp_system' AND table_name='accounts' AND column_name='version') THEN
        ALTER TABLE accounts ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='erp_system' AND table_name='payment_vouchers' AND column_name='version') THEN
        ALTER TABLE payment_vouchers ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='erp_system' AND table_name='receipt_vouchers' AND column_name='version') THEN
        ALTER TABLE receipt_vouchers ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
    END IF;
END $$;

-- ============================================================
-- Reconciliation: remaining_amount for partial multi-match
-- ============================================================
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='erp_system' AND table_name='reconciliation_lines' AND column_name='remaining_amount') THEN
        ALTER TABLE reconciliation_lines ADD COLUMN remaining_amount NUMERIC(19, 2);
    END IF;
END $$;

UPDATE reconciliation_lines SET remaining_amount = amount WHERE remaining_amount IS NULL;


-- ---------- source: V28__reconciliation_match_pairs.sql ----------
SET search_path TO erp_system, public;

CREATE TABLE IF NOT EXISTS reconciliation_match_pairs (
    id                  BIGSERIAL PRIMARY KEY,
    reconciliation_id   BIGINT NOT NULL REFERENCES reconciliations(id),
    statement_line_id   BIGINT NOT NULL REFERENCES reconciliation_lines(id),
    system_line_id      BIGINT NOT NULL REFERENCES reconciliation_lines(id),
    matched_amount      NUMERIC(19, 2) NOT NULL,
    matched_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    matched_by          VARCHAR(100) NOT NULL,
    active              BOOLEAN NOT NULL DEFAULT TRUE,
    unmatched_at        TIMESTAMPTZ,
    unmatched_by        VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_match_pairs_recon ON reconciliation_match_pairs (reconciliation_id);
CREATE INDEX IF NOT EXISTS idx_match_pairs_stmt ON reconciliation_match_pairs (statement_line_id);
CREATE INDEX IF NOT EXISTS idx_match_pairs_sys ON reconciliation_match_pairs (system_line_id);
CREATE INDEX IF NOT EXISTS idx_match_pairs_active ON reconciliation_match_pairs (reconciliation_id, active);


-- ---------- source: V29__fix_voucher_nullable_columns.sql ----------
-- Fix: journal_entry_id must be nullable on vouchers because drafts have no journal entry yet
ALTER TABLE erp_system.payment_vouchers ALTER COLUMN journal_entry_id DROP NOT NULL;
ALTER TABLE erp_system.receipt_vouchers ALTER COLUMN journal_entry_id DROP NOT NULL;

-- Fix: account_id column added in V4 is NOT NULL but not mapped by JPA entities.
-- Make it nullable so Hibernate inserts don't fail, or drop if unused.
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'erp_system' AND table_name = 'payment_vouchers' AND column_name = 'account_id'
    ) THEN
        ALTER TABLE erp_system.payment_vouchers ALTER COLUMN account_id DROP NOT NULL;
        ALTER TABLE erp_system.payment_vouchers ALTER COLUMN account_id SET DEFAULT NULL;
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'erp_system' AND table_name = 'receipt_vouchers' AND column_name = 'account_id'
    ) THEN
        ALTER TABLE erp_system.receipt_vouchers ALTER COLUMN account_id DROP NOT NULL;
        ALTER TABLE erp_system.receipt_vouchers ALTER COLUMN account_id SET DEFAULT NULL;
    END IF;
END $$;


-- ---------- source: V30__sync_legacy_account_name.sql ----------
SET search_path TO erp_system, public;

UPDATE accounts
SET
    name_en = COALESCE(NULLIF(BTRIM(name_en), ''), NULLIF(BTRIM(name), '')),
    name_ar = COALESCE(NULLIF(BTRIM(name_ar), ''), NULLIF(BTRIM(name_en), ''), NULLIF(BTRIM(name), '')),
    name = COALESCE(NULLIF(BTRIM(name), ''), NULLIF(BTRIM(name_en), ''), NULLIF(BTRIM(name_ar), ''), code)
WHERE
    name IS NULL OR BTRIM(name) = ''
    OR name_en IS NULL OR BTRIM(name_en) = ''
    OR name_ar IS NULL OR BTRIM(name_ar) = '';


-- ---------- source: V31__accounts_name_compatibility_trigger.sql ----------
SET search_path TO erp_system, public;

CREATE OR REPLACE FUNCTION erp_system.sync_accounts_name_legacy()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    NEW.name_en := COALESCE(NULLIF(BTRIM(NEW.name_en), ''), NULLIF(BTRIM(NEW.name), ''), NULLIF(BTRIM(NEW.name_ar), ''), NEW.code);
    NEW.name_ar := COALESCE(NULLIF(BTRIM(NEW.name_ar), ''), NEW.name_en);
    NEW.name := COALESCE(NULLIF(BTRIM(NEW.name), ''), NEW.name_en, NEW.name_ar, NEW.code);

    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_accounts_sync_legacy_name ON accounts;

CREATE TRIGGER trg_accounts_sync_legacy_name
BEFORE INSERT OR UPDATE ON accounts
FOR EACH ROW
EXECUTE FUNCTION erp_system.sync_accounts_name_legacy();

UPDATE accounts
SET
    name_en = COALESCE(NULLIF(BTRIM(name_en), ''), NULLIF(BTRIM(name), ''), NULLIF(BTRIM(name_ar), ''), code),
    name_ar = COALESCE(NULLIF(BTRIM(name_ar), ''), NULLIF(BTRIM(name_en), ''), NULLIF(BTRIM(name), ''), code),
    name = COALESCE(NULLIF(BTRIM(name), ''), NULLIF(BTRIM(name_en), ''), NULLIF(BTRIM(name_ar), ''), code)
WHERE
    name IS NULL OR BTRIM(name) = ''
    OR name_en IS NULL OR BTRIM(name_en) = ''
    OR name_ar IS NULL OR BTRIM(name_ar) = '';


-- ---------- source: V32__accounting_reseed_revenue_and_remove_transfers.sql ----------
SET search_path TO erp_system, public;

DELETE FROM role_menu_permissions
WHERE menu_item_id = 'transfers';

DELETE FROM ui_menu_items
WHERE id = 'transfers';

DELETE FROM lookup_values
WHERE type_code = 'transfer-statuses';

DELETE FROM lookup_types
WHERE code = 'transfer-statuses';

UPDATE lookup_values
SET code = 'REVENUE',
    name_en = 'Revenue',
    name_ar = 'Ø§Ù„Ø¥ÙŠØ±Ø§Ø¯Ø§Øª',
    sort_order = 4,
    updated_by = 'flyway',
    updated_at = NOW()
WHERE type_code = 'account-types'
  AND code = 'INCOME';

INSERT INTO lookup_values (type_code, code, sort_order, is_active, name_en, name_ar, created_by, updated_by)
SELECT 'account-types', 'REVENUE', 4, TRUE, 'Revenue', 'Ø§Ù„Ø¥ÙŠØ±Ø§Ø¯Ø§Øª', 'flyway', 'flyway'
WHERE NOT EXISTS (
    SELECT 1
    FROM lookup_values
    WHERE type_code = 'account-types'
      AND code = 'REVENUE'
);

UPDATE ui_menu_items
SET title_key = 'NAV.JOURNAL_VOUCHERS'
WHERE id = 'journal-entries';

ALTER TABLE erp_system.accounts
DROP CONSTRAINT IF EXISTS chk_accounts_type;

TRUNCATE TABLE
    reconciliation_match_pairs,
    reconciliation_lines,
    reconciliations,
    checks,
    transactions,
    transfers,
    payment_vouchers,
    receipt_vouchers,
    bill_lines,
    bills,
    customer_invoice_lines,
    customer_invoices,
    budgets,
    bank_accounts,
    journal_entry_lines,
    journal_entries,
    accounting_audit_log,
    accounts
RESTART IDENTITY CASCADE;

INSERT INTO accounts (code, name, name_en, name_ar, parent_id, account_type, level, full_path, is_active, is_postable, opening_balance, opening_balance_side, created_by, updated_by, version)
VALUES
    ('1000', 'Assets', 'Assets', 'Ø§Ù„Ø£ØµÙˆÙ„', NULL, 'ASSET', 1, 'Assets', TRUE, FALSE, 0, 'DEBIT', 'flyway', 'flyway', 0),
    ('2000', 'Liabilities', 'Liabilities', 'Ø§Ù„Ø§Ù„ØªØ²Ø§Ù…Ø§Øª', NULL, 'LIABILITY', 1, 'Liabilities', TRUE, FALSE, 0, 'CREDIT', 'flyway', 'flyway', 0),
    ('3000', 'Equity', 'Equity', 'Ø­Ù‚ÙˆÙ‚ Ø§Ù„Ù…Ù„ÙƒÙŠØ©', NULL, 'EQUITY', 1, 'Equity', TRUE, FALSE, 0, 'CREDIT', 'flyway', 'flyway', 0),
    ('4000', 'Revenue', 'Revenue', 'Ø§Ù„Ø¥ÙŠØ±Ø§Ø¯Ø§Øª', NULL, 'REVENUE', 1, 'Revenue', TRUE, FALSE, 0, 'CREDIT', 'flyway', 'flyway', 0),
    ('5000', 'Expenses', 'Expenses', 'Ø§Ù„Ù…ØµØ±ÙˆÙØ§Øª', NULL, 'EXPENSE', 1, 'Expenses', TRUE, FALSE, 0, 'DEBIT', 'flyway', 'flyway', 0);

INSERT INTO accounts (code, name, name_en, name_ar, parent_id, account_type, level, full_path, is_active, is_postable, opening_balance, opening_balance_side, created_by, updated_by, version)
SELECT '1110', 'Cash on Hand', 'Cash on Hand', 'Ø§Ù„ØµÙ†Ø¯ÙˆÙ‚', parent.id, 'ASSET', 2, 'Assets/Cash on Hand', TRUE, TRUE, 0, 'DEBIT', 'flyway', 'flyway', 0
FROM accounts parent
WHERE parent.code = '1000';

INSERT INTO accounts (code, name, name_en, name_ar, parent_id, account_type, level, full_path, is_active, is_postable, opening_balance, opening_balance_side, created_by, updated_by, version)
SELECT '1120', 'Main Bank Account', 'Main Bank Account', 'Ø§Ù„Ø­Ø³Ø§Ø¨ Ø§Ù„Ø¨Ù†ÙƒÙŠ Ø§Ù„Ø±Ø¦ÙŠØ³ÙŠ', parent.id, 'ASSET', 2, 'Assets/Main Bank Account', TRUE, TRUE, 0, 'DEBIT', 'flyway', 'flyway', 0
FROM accounts parent
WHERE parent.code = '1000';

INSERT INTO accounts (code, name, name_en, name_ar, parent_id, account_type, level, full_path, is_active, is_postable, opening_balance, opening_balance_side, created_by, updated_by, version)
SELECT '1210', 'Accounts Receivable', 'Accounts Receivable', 'Ø§Ù„Ø°Ù…Ù… Ø§Ù„Ù…Ø¯ÙŠÙ†Ø©', parent.id, 'ASSET', 2, 'Assets/Accounts Receivable', TRUE, TRUE, 0, 'DEBIT', 'flyway', 'flyway', 0
FROM accounts parent
WHERE parent.code = '1000';

INSERT INTO accounts (code, name, name_en, name_ar, parent_id, account_type, level, full_path, is_active, is_postable, opening_balance, opening_balance_side, created_by, updated_by, version)
SELECT '2110', 'Accounts Payable', 'Accounts Payable', 'Ø§Ù„Ø°Ù…Ù… Ø§Ù„Ø¯Ø§Ø¦Ù†Ø©', parent.id, 'LIABILITY', 2, 'Liabilities/Accounts Payable', TRUE, TRUE, 0, 'CREDIT', 'flyway', 'flyway', 0
FROM accounts parent
WHERE parent.code = '2000';

INSERT INTO accounts (code, name, name_en, name_ar, parent_id, account_type, level, full_path, is_active, is_postable, opening_balance, opening_balance_side, created_by, updated_by, version)
SELECT '2210', 'Tax Payable', 'Tax Payable', 'Ø¶Ø±ÙŠØ¨Ø© Ù…Ø³ØªØ­Ù‚Ø©', parent.id, 'LIABILITY', 2, 'Liabilities/Tax Payable', TRUE, TRUE, 0, 'CREDIT', 'flyway', 'flyway', 0
FROM accounts parent
WHERE parent.code = '2000';

INSERT INTO accounts (code, name, name_en, name_ar, parent_id, account_type, level, full_path, is_active, is_postable, opening_balance, opening_balance_side, created_by, updated_by, version)
SELECT '3110', 'Owner Capital', 'Owner Capital', 'Ø±Ø£Ø³ Ø§Ù„Ù…Ø§Ù„', parent.id, 'EQUITY', 2, 'Equity/Owner Capital', TRUE, TRUE, 0, 'CREDIT', 'flyway', 'flyway', 0
FROM accounts parent
WHERE parent.code = '3000';

INSERT INTO accounts (code, name, name_en, name_ar, parent_id, account_type, level, full_path, is_active, is_postable, opening_balance, opening_balance_side, created_by, updated_by, version)
SELECT '3120', 'Retained Earnings', 'Retained Earnings', 'Ø§Ù„Ø£Ø±Ø¨Ø§Ø­ Ø§Ù„Ù…Ø­ØªØ¬Ø²Ø©', parent.id, 'EQUITY', 2, 'Equity/Retained Earnings', TRUE, TRUE, 0, 'CREDIT', 'flyway', 'flyway', 0
FROM accounts parent
WHERE parent.code = '3000';

INSERT INTO accounts (code, name, name_en, name_ar, parent_id, account_type, level, full_path, is_active, is_postable, opening_balance, opening_balance_side, created_by, updated_by, version)
SELECT '4110', 'Sales Revenue', 'Sales Revenue', 'Ø¥ÙŠØ±Ø§Ø¯Ø§Øª Ø§Ù„Ù…Ø¨ÙŠØ¹Ø§Øª', parent.id, 'REVENUE', 2, 'Revenue/Sales Revenue', TRUE, TRUE, 0, 'CREDIT', 'flyway', 'flyway', 0
FROM accounts parent
WHERE parent.code = '4000';

INSERT INTO accounts (code, name, name_en, name_ar, parent_id, account_type, level, full_path, is_active, is_postable, opening_balance, opening_balance_side, created_by, updated_by, version)
SELECT '4120', 'Service Revenue', 'Service Revenue', 'Ø¥ÙŠØ±Ø§Ø¯Ø§Øª Ø§Ù„Ø®Ø¯Ù…Ø§Øª', parent.id, 'REVENUE', 2, 'Revenue/Service Revenue', TRUE, TRUE, 0, 'CREDIT', 'flyway', 'flyway', 0
FROM accounts parent
WHERE parent.code = '4000';

INSERT INTO accounts (code, name, name_en, name_ar, parent_id, account_type, level, full_path, is_active, is_postable, opening_balance, opening_balance_side, created_by, updated_by, version)
SELECT '5110', 'Operating Expenses', 'Operating Expenses', 'Ø§Ù„Ù…ØµØ±ÙˆÙØ§Øª Ø§Ù„ØªØ´ØºÙŠÙ„ÙŠØ©', parent.id, 'EXPENSE', 2, 'Expenses/Operating Expenses', TRUE, TRUE, 0, 'DEBIT', 'flyway', 'flyway', 0
FROM accounts parent
WHERE parent.code = '5000';

INSERT INTO accounts (code, name, name_en, name_ar, parent_id, account_type, level, full_path, is_active, is_postable, opening_balance, opening_balance_side, created_by, updated_by, version)
SELECT '5120', 'Office Supplies Expense', 'Office Supplies Expense', 'Ù…ØµØ±ÙˆÙØ§Øª Ù…ÙƒØªØ¨ÙŠØ©', parent.id, 'EXPENSE', 2, 'Expenses/Office Supplies Expense', TRUE, TRUE, 0, 'DEBIT', 'flyway', 'flyway', 0
FROM accounts parent
WHERE parent.code = '5000';

INSERT INTO accounts (code, name, name_en, name_ar, parent_id, account_type, level, full_path, is_active, is_postable, opening_balance, opening_balance_side, created_by, updated_by, version)
SELECT '5130', 'Cost of Goods Sold', 'Cost of Goods Sold', 'ØªÙƒÙ„ÙØ© Ø§Ù„Ø¨Ø¶Ø§Ø¹Ø© Ø§Ù„Ù…Ø¨Ø§Ø¹Ø©', parent.id, 'EXPENSE', 2, 'Expenses/Cost of Goods Sold', TRUE, TRUE, 0, 'DEBIT', 'flyway', 'flyway', 0
FROM accounts parent
WHERE parent.code = '5000';

ALTER TABLE erp_system.accounts
ADD CONSTRAINT chk_accounts_type
CHECK (account_type IN ('ASSET', 'LIABILITY', 'EQUITY', 'REVENUE', 'EXPENSE'));

INSERT INTO bank_accounts (bank_name, account_number, iban, currency, opening_balance, current_balance, linked_account_id, is_active, created_by, updated_by)
SELECT 'Main Operating Bank', 'ERP-BANK-001', NULL, 'USD', 0, 0, account.id, TRUE, 'flyway', 'flyway'
FROM accounts account
WHERE account.code = '1120';


-- ---------- source: V33__add_general_ledger_menu_item.sql ----------
SET search_path TO erp_system, public;

INSERT INTO ui_menu_items (id, parent_id, sort_order, item_type, title_key, icon, url, is_external, target_blank, roles_csv, item_classes, breadcrumbs_flag)
VALUES ('general-ledger', 'hesabaty', 2, 'item', 'NAV.GENERAL_LEDGER', 'auto_stories', '/general-ledger', FALSE, FALSE, 'ADMIN,ACCOUNTANT', 'nav-item', FALSE)
ON CONFLICT (id) DO UPDATE
SET parent_id       = EXCLUDED.parent_id,
    sort_order      = EXCLUDED.sort_order,
    item_type       = EXCLUDED.item_type,
    title_key       = EXCLUDED.title_key,
    icon            = EXCLUDED.icon,
    url             = EXCLUDED.url,
    roles_csv       = EXCLUDED.roles_csv,
    item_classes    = EXCLUDED.item_classes;

UPDATE ui_menu_items SET sort_order = sort_order + 1
WHERE parent_id = 'hesabaty'
  AND id <> 'general-ledger'
  AND id <> 'dashboard'
  AND id <> 'accounts'
  AND sort_order >= 2;


-- ---------- source: V34__restructure_admin_menu_items.sql ----------
SET search_path TO erp_system, public;

DELETE FROM ui_menu_items
WHERE id = 'accountants';

INSERT INTO ui_menu_items (id, parent_id, sort_order, item_type, title_key, icon, url, is_external, target_blank, roles_csv, item_classes, breadcrumbs_flag)
VALUES
    ('admin-group', 'hesabaty', 14, 'collapse', 'NAV.SYSTEM_MANAGEMENT', 'admin_panel_settings', NULL, FALSE, FALSE, 'ADMIN', NULL, FALSE),
    ('admin-users', 'admin-group', 0, 'item', 'NAV.USERS', 'people', '/accountants/users', FALSE, FALSE, 'ADMIN', 'nav-item', FALSE),
    ('admin-roles', 'admin-group', 1, 'item', 'NAV.ROLES', 'shield', '/accountants/roles', FALSE, FALSE, 'ADMIN', 'nav-item', FALSE),
    ('admin-lookups', 'admin-group', 2, 'item', 'NAV.LOOKUPS', 'list_alt', '/accountants/lookups', FALSE, FALSE, 'ADMIN', 'nav-item', FALSE);

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
JOIN ui_menu_items menu ON menu.id IN ('admin-group', 'admin-users', 'admin-roles', 'admin-lookups')
WHERE role.code = 'ADMIN'
ON CONFLICT (role_id, menu_item_id) DO UPDATE
SET can_view   = EXCLUDED.can_view,
    can_create = EXCLUDED.can_create,
    can_edit   = EXCLUDED.can_edit,
    can_delete = EXCLUDED.can_delete,
    updated_by = 'flyway';


-- ---------- source: V35__approval_instead_of_posting.sql ----------
-- Align voucher and journal workflow with approval-only UX: remove POSTED voucher status;
-- journal entries use APPROVED instead of POSTED for finalized (in-GL) entries.

SET search_path TO erp_system, public;

UPDATE payment_vouchers SET status = 'APPROVED' WHERE status = 'POSTED';
UPDATE receipt_vouchers SET status = 'APPROVED' WHERE status = 'POSTED';
UPDATE journal_entries SET status = 'APPROVED' WHERE status = 'POSTED';

DO $$
DECLARE
  r RECORD;
BEGIN
  FOR r IN
    SELECT c.conname, cl.relname
    FROM pg_constraint c
    JOIN pg_class cl ON c.conrelid = cl.oid
    JOIN pg_namespace n ON cl.relnamespace = n.oid
    WHERE n.nspname = 'erp_system'
      AND cl.relname IN ('payment_vouchers', 'receipt_vouchers', 'journal_entries')
      AND c.contype = 'c'
      AND pg_get_constraintdef(c.oid) ILIKE '%status%'
  LOOP
    EXECUTE format('ALTER TABLE erp_system.%I DROP CONSTRAINT %I', r.relname, r.conname);
  END LOOP;
END $$;

ALTER TABLE payment_vouchers
  ADD CONSTRAINT payment_vouchers_status_check CHECK (status IN ('DRAFT', 'APPROVED', 'CANCELLED'));

ALTER TABLE receipt_vouchers
  ADD CONSTRAINT receipt_vouchers_status_check CHECK (status IN ('DRAFT', 'APPROVED', 'CANCELLED'));

ALTER TABLE journal_entries
  ADD CONSTRAINT chk_journal_entries_status CHECK (status IN ('DRAFT', 'APPROVED', 'REVERSED', 'CANCELLED'));

DELETE FROM lookup_values WHERE type_code = 'voucher-statuses' AND code = 'POSTED';

UPDATE lookup_values
SET code = 'APPROVED',
    sort_order = 2,
    updated_at = NOW(),
    updated_by = 'flyway'
WHERE type_code = 'journal-entry-statuses' AND code = 'POSTED';


-- ---------- source: V36__admin_screens_menu_item.sql ----------
SET search_path TO erp_system, public;

INSERT INTO ui_menu_items (id, parent_id, sort_order, item_type, title_key, icon, url, is_external, target_blank, roles_csv, item_classes, breadcrumbs_flag)
VALUES ('admin-screens', 'admin-group', 3, 'item', 'NAV.SCREENS', 'view_quilt', '/accountants/screens', FALSE, FALSE, 'ADMIN', 'nav-item', FALSE)
ON CONFLICT (id) DO NOTHING;

INSERT INTO role_menu_permissions (role_id, menu_item_id, can_view, can_create, can_edit, can_delete, created_by, updated_by)
SELECT role.id,
       'admin-screens',
       TRUE,
       TRUE,
       TRUE,
       TRUE,
       'flyway',
       'flyway'
FROM access_roles role
WHERE role.code = 'ADMIN'
ON CONFLICT (role_id, menu_item_id) DO UPDATE
SET can_view   = EXCLUDED.can_view,
    can_create = EXCLUDED.can_create,
    can_edit   = EXCLUDED.can_edit,
    can_delete = EXCLUDED.can_delete,
    updated_by = 'flyway';


-- ---------- source: V37__account_types_lookup_bilingual_labels.sql ----------
SET search_path TO erp_system, public;

-- Ensure account-type lookup values have correct English + Arabic labels (admin UI lists both in DB; app shows one column by locale).
UPDATE lookup_values
SET name_en = 'Asset',
    name_ar = 'Ø§Ù„Ø£ØµÙˆÙ„',
    updated_by = 'flyway',
    updated_at = NOW()
WHERE type_code = 'account-types'
  AND code = 'ASSET';

UPDATE lookup_values
SET name_en = 'Liability',
    name_ar = 'Ø§Ù„Ø§Ù„ØªØ²Ø§Ù…Ø§Øª',
    updated_by = 'flyway',
    updated_at = NOW()
WHERE type_code = 'account-types'
  AND code = 'LIABILITY';

UPDATE lookup_values
SET name_en = 'Equity',
    name_ar = 'Ø­Ù‚ÙˆÙ‚ Ø§Ù„Ù…Ù„ÙƒÙŠØ©',
    updated_by = 'flyway',
    updated_at = NOW()
WHERE type_code = 'account-types'
  AND code = 'EQUITY';

UPDATE lookup_values
SET name_en = 'Revenue',
    name_ar = 'Ø§Ù„Ø¥ÙŠØ±Ø§Ø¯Ø§Øª',
    updated_by = 'flyway',
    updated_at = NOW()
WHERE type_code = 'account-types'
  AND code IN ('REVENUE', 'INCOME');

UPDATE lookup_values
SET name_en = 'Expense',
    name_ar = 'Ø§Ù„Ù…ØµØ±ÙˆÙØ§Øª',
    updated_by = 'flyway',
    updated_at = NOW()
WHERE type_code = 'account-types'
  AND code = 'EXPENSE';


-- ---------- source: V38__drop_accounts_is_postable.sql ----------
SET search_path TO erp_system, public;

ALTER TABLE accounts DROP COLUMN IF EXISTS is_postable;


-- ---------- source: V39__comprehensive_demo_data.sql ----------
SET search_path TO erp_system, public;

-- ============================================================
-- V39: Comprehensive demo data for all screens
-- Ensures every screen has realistic data for the demo
-- ============================================================

-- Customer Invoices (varied statuses and dates)
INSERT INTO customer_invoices (
    invoice_number, invoice_date, due_date, customer_name, customer_reference,
    description, subtotal, tax_amount, total_amount, paid_amount, outstanding_amount,
    status, receivable_account_id, revenue_account_id, created_by, updated_by
)
SELECT 'INV-2026-001', CURRENT_DATE - 45, CURRENT_DATE - 15,
       'Global Trading LLC', 'GT-REF-100',
       'Consulting services for Q1 2026', 12000.00, 600.00, 12600.00, 12600.00, 0.00,
       'PAID', ar.id, rev.id, 'flyway', 'flyway'
FROM accounts ar JOIN accounts rev ON rev.code = '4100' WHERE ar.code = '1200'
  AND NOT EXISTS (SELECT 1 FROM customer_invoices ci WHERE ci.invoice_number = 'INV-2026-001');

INSERT INTO customer_invoices (
    invoice_number, invoice_date, due_date, customer_name, customer_reference,
    description, subtotal, tax_amount, total_amount, paid_amount, outstanding_amount,
    status, receivable_account_id, revenue_account_id, created_by, updated_by
)
SELECT 'INV-2026-002', CURRENT_DATE - 30, CURRENT_DATE - 1,
       'Blue Ocean Technologies', 'BOT-2026-Q1',
       'Software development - Phase 1', 25000.00, 1250.00, 26250.00, 15000.00, 11250.00,
       'PARTIAL', ar.id, rev.id, 'flyway', 'flyway'
FROM accounts ar JOIN accounts rev ON rev.code = '4200' WHERE ar.code = '1200'
  AND NOT EXISTS (SELECT 1 FROM customer_invoices ci WHERE ci.invoice_number = 'INV-2026-002');

INSERT INTO customer_invoices (
    invoice_number, invoice_date, due_date, customer_name, customer_reference,
    description, subtotal, tax_amount, total_amount, paid_amount, outstanding_amount,
    status, receivable_account_id, revenue_account_id,
    posted_at, posted_by, created_by, updated_by
)
SELECT 'INV-2026-003', CURRENT_DATE - 15, CURRENT_DATE + 15,
       'Desert Star Enterprises', 'DSE-ADV-07',
       'IT infrastructure setup', 8500.00, 425.00, 8925.00, 0.00, 8925.00,
       'POSTED', ar.id, rev.id, NOW() - INTERVAL '14 days', 'admin', 'flyway', 'flyway'
FROM accounts ar JOIN accounts rev ON rev.code = '4200' WHERE ar.code = '1200'
  AND NOT EXISTS (SELECT 1 FROM customer_invoices ci WHERE ci.invoice_number = 'INV-2026-003');

INSERT INTO customer_invoices (
    invoice_number, invoice_date, due_date, customer_name, customer_reference,
    description, subtotal, tax_amount, total_amount, paid_amount, outstanding_amount,
    status, receivable_account_id, revenue_account_id, created_by, updated_by
)
SELECT 'INV-2026-004', CURRENT_DATE - 5, CURRENT_DATE + 25,
       'Al Manara Group', 'AMG-2026-011',
       'Monthly maintenance contract', 3500.00, 175.00, 3675.00, 0.00, 3675.00,
       'DRAFT', ar.id, rev.id, 'flyway', 'flyway'
FROM accounts ar JOIN accounts rev ON rev.code = '4100' WHERE ar.code = '1200'
  AND NOT EXISTS (SELECT 1 FROM customer_invoices ci WHERE ci.invoice_number = 'INV-2026-004');

INSERT INTO customer_invoices (
    invoice_number, invoice_date, due_date, customer_name, customer_reference,
    description, subtotal, tax_amount, total_amount, paid_amount, outstanding_amount,
    status, receivable_account_id, revenue_account_id,
    cancelled_at, cancelled_by, created_by, updated_by
)
SELECT 'INV-2026-005', CURRENT_DATE - 60, CURRENT_DATE - 30,
       'Phoenix Industries', 'PHX-OLD-009',
       'Cancelled order - equipment supply', 6200.00, 310.00, 6510.00, 0.00, 0.00,
       'CANCELLED', ar.id, rev.id, NOW() - INTERVAL '28 days', 'admin', 'flyway', 'flyway'
FROM accounts ar JOIN accounts rev ON rev.code = '4100' WHERE ar.code = '1200'
  AND NOT EXISTS (SELECT 1 FROM customer_invoices ci WHERE ci.invoice_number = 'INV-2026-005');

-- Checks (issued and received, various statuses)
INSERT INTO checks (check_number, check_type, bank_name, issue_date, due_date, amount, status, party_name, created_by, updated_by)
SELECT 'CHK-ISS-001', 'ISSUED', 'Emirates NBD', CURRENT_DATE - 20, CURRENT_DATE - 5, 3200.00, 'CLEARED', 'Team Payroll', 'flyway', 'flyway'
WHERE NOT EXISTS (SELECT 1 FROM checks c WHERE c.check_number = 'CHK-ISS-001');

INSERT INTO checks (check_number, check_type, bank_name, issue_date, due_date, amount, status, party_name, created_by, updated_by)
SELECT 'CHK-ISS-002', 'ISSUED', 'Emirates NBD', CURRENT_DATE - 10, CURRENT_DATE + 5, 1700.00, 'PENDING', 'Al Salam Properties', 'flyway', 'flyway'
WHERE NOT EXISTS (SELECT 1 FROM checks c WHERE c.check_number = 'CHK-ISS-002');

INSERT INTO checks (check_number, check_type, bank_name, issue_date, due_date, amount, status, party_name, created_by, updated_by)
SELECT 'CHK-ISS-003', 'ISSUED', 'Abu Dhabi Islamic Bank', CURRENT_DATE - 5, CURRENT_DATE + 25, 2450.00, 'DEPOSITED', 'Office Mart LLC', 'flyway', 'flyway'
WHERE NOT EXISTS (SELECT 1 FROM checks c WHERE c.check_number = 'CHK-ISS-003');

INSERT INTO checks (check_number, check_type, bank_name, issue_date, due_date, amount, status, party_name, created_by, updated_by)
SELECT 'CHK-RCV-001', 'RECEIVED', 'National Bank of Abu Dhabi', CURRENT_DATE - 15, CURRENT_DATE - 2, 4400.00, 'CLEARED', 'Acme Services Client', 'flyway', 'flyway'
WHERE NOT EXISTS (SELECT 1 FROM checks c WHERE c.check_number = 'CHK-RCV-001');

INSERT INTO checks (check_number, check_type, bank_name, issue_date, due_date, amount, status, party_name, created_by, updated_by)
SELECT 'CHK-RCV-002', 'RECEIVED', 'Dubai Islamic Bank', CURRENT_DATE - 8, CURRENT_DATE + 10, 1850.00, 'DEPOSITED', 'Blue Ocean Trading', 'flyway', 'flyway'
WHERE NOT EXISTS (SELECT 1 FROM checks c WHERE c.check_number = 'CHK-RCV-002');

INSERT INTO checks (check_number, check_type, bank_name, issue_date, due_date, amount, status, party_name, created_by, updated_by)
SELECT 'CHK-RCV-003', 'RECEIVED', 'Emirates NBD', CURRENT_DATE - 30, CURRENT_DATE - 15, 950.00, 'BOUNCED', 'Quick Fix Supplies', 'flyway', 'flyway'
WHERE NOT EXISTS (SELECT 1 FROM checks c WHERE c.check_number = 'CHK-RCV-003');

INSERT INTO checks (check_number, check_type, bank_name, issue_date, due_date, amount, status, party_name, created_by, updated_by)
SELECT 'CHK-ISS-004', 'ISSUED', 'Emirates NBD', CURRENT_DATE - 40, CURRENT_DATE - 25, 500.00, 'CANCELLED', 'Cancelled Vendor', 'flyway', 'flyway'
WHERE NOT EXISTS (SELECT 1 FROM checks c WHERE c.check_number = 'CHK-ISS-004');

INSERT INTO checks (check_number, check_type, bank_name, issue_date, due_date, amount, status, party_name, created_by, updated_by)
SELECT 'CHK-RCV-004', 'RECEIVED', 'Abu Dhabi Islamic Bank', CURRENT_DATE - 3, CURRENT_DATE + 30, 5200.00, 'PENDING', 'Desert Star Enterprises', 'flyway', 'flyway'
WHERE NOT EXISTS (SELECT 1 FROM checks c WHERE c.check_number = 'CHK-RCV-004');

-- Additional Transactions for richer history
INSERT INTO transactions (
    transaction_date, reference, description, transaction_type, status, amount,
    debit_account_id, credit_account_id, related_document_reference, created_by, updated_by
)
SELECT CURRENT_DATE - 45, 'TX-DEMO-0003', 'Invoice payment received from Global Trading',
       'SALE', 'COMPLETED', 12600.00, debit_acc.id, credit_acc.id, 'INV-2026-001', 'flyway', 'flyway'
FROM accounts debit_acc JOIN accounts credit_acc ON credit_acc.code = '4100'
WHERE debit_acc.code = '1120'
  AND NOT EXISTS (SELECT 1 FROM transactions t WHERE t.reference = 'TX-DEMO-0003');

INSERT INTO transactions (
    transaction_date, reference, description, transaction_type, status, amount,
    debit_account_id, credit_account_id, related_document_reference, created_by, updated_by
)
SELECT CURRENT_DATE - 30, 'TX-DEMO-0004', 'Partial payment from Blue Ocean Technologies',
       'SALE', 'COMPLETED', 15000.00, debit_acc.id, credit_acc.id, 'INV-2026-002', 'flyway', 'flyway'
FROM accounts debit_acc JOIN accounts credit_acc ON credit_acc.code = '4200'
WHERE debit_acc.code = '1120'
  AND NOT EXISTS (SELECT 1 FROM transactions t WHERE t.reference = 'TX-DEMO-0004');

INSERT INTO transactions (
    transaction_date, reference, description, transaction_type, status, amount,
    debit_account_id, credit_account_id, related_document_reference, created_by, updated_by
)
SELECT CURRENT_DATE - 10, 'TX-DEMO-0005', 'Rent payment for office lease',
       'PURCHASE', 'COMPLETED', 1700.00, debit_acc.id, credit_acc.id, 'PV-DEMO-002', 'flyway', 'flyway'
FROM accounts debit_acc JOIN accounts credit_acc ON credit_acc.code = '1120'
WHERE debit_acc.code = '5200'
  AND NOT EXISTS (SELECT 1 FROM transactions t WHERE t.reference = 'TX-DEMO-0005');

INSERT INTO transactions (
    transaction_date, reference, description, transaction_type, status, amount,
    debit_account_id, credit_account_id, related_document_reference, created_by, updated_by
)
SELECT CURRENT_DATE - 3, 'TX-DEMO-0006', 'Utilities payment - electricity and water',
       'PURCHASE', 'COMPLETED', 400.00, debit_acc.id, credit_acc.id, 'UTIL-MAR', 'flyway', 'flyway'
FROM accounts debit_acc JOIN accounts credit_acc ON credit_acc.code = '1110'
WHERE debit_acc.code = '5300'
  AND NOT EXISTS (SELECT 1 FROM transactions t WHERE t.reference = 'TX-DEMO-0006');

INSERT INTO transactions (
    transaction_date, reference, description, transaction_type, status, amount,
    debit_account_id, credit_account_id, related_document_reference, created_by, updated_by
)
SELECT CURRENT_DATE - 1, 'TX-DEMO-0007', 'Inventory restocking purchase',
       'PURCHASE', 'PENDING', 4500.00, debit_acc.id, credit_acc.id, 'PO-2026-015', 'flyway', 'flyway'
FROM accounts debit_acc JOIN accounts credit_acc ON credit_acc.code = '2100'
WHERE debit_acc.code = '1300'
  AND NOT EXISTS (SELECT 1 FROM transactions t WHERE t.reference = 'TX-DEMO-0007');

INSERT INTO transactions (
    transaction_date, reference, description, transaction_type, status, amount,
    debit_account_id, credit_account_id, related_document_reference, created_by, updated_by
)
SELECT CURRENT_DATE, 'TX-DEMO-0008', 'Office supplies purchase',
       'PURCHASE', 'PENDING', 650.00, debit_acc.id, credit_acc.id, 'BILL-2026-044', 'flyway', 'flyway'
FROM accounts debit_acc JOIN accounts credit_acc ON credit_acc.code = '1120'
WHERE debit_acc.code = '5400'
  AND NOT EXISTS (SELECT 1 FROM transactions t WHERE t.reference = 'TX-DEMO-0008');

-- Additional journal entries (DRAFT and APPROVED statuses for variety)
INSERT INTO journal_entries (
    entry_date, reference_number, description, status, total_debit, total_credit,
    source_module, currency_code, entry_type, created_by, updated_by
)
SELECT CURRENT_DATE - 2, 'JE-DEMO-0005', 'Pending inventory adjustment', 'DRAFT', 1500.00, 1500.00,
       'MANUAL', 'USD', 'MANUAL', 'flyway', 'flyway'
WHERE NOT EXISTS (SELECT 1 FROM journal_entries je WHERE je.reference_number = 'JE-DEMO-0005');

INSERT INTO journal_entry_lines (journal_entry_id, line_number, account_id, debit, credit, description, created_by, updated_by)
SELECT je.id, 1, acc.id, 1500.00, 0.00, 'Inventory increase', 'flyway', 'flyway'
FROM journal_entries je JOIN accounts acc ON acc.code = '1300'
WHERE je.reference_number = 'JE-DEMO-0005'
  AND NOT EXISTS (SELECT 1 FROM journal_entry_lines l WHERE l.journal_entry_id = je.id AND l.line_number = 1);

INSERT INTO journal_entry_lines (journal_entry_id, line_number, account_id, debit, credit, description, created_by, updated_by)
SELECT je.id, 2, acc.id, 0.00, 1500.00, 'Accounts payable increase', 'flyway', 'flyway'
FROM journal_entries je JOIN accounts acc ON acc.code = '2100'
WHERE je.reference_number = 'JE-DEMO-0005'
  AND NOT EXISTS (SELECT 1 FROM journal_entry_lines l WHERE l.journal_entry_id = je.id AND l.line_number = 2);

INSERT INTO journal_entries (
    entry_date, reference_number, description, status, total_debit, total_credit,
    posted_at, posted_by, source_module, currency_code, entry_type, created_by, updated_by
)
SELECT CURRENT_DATE - 8, 'JE-DEMO-0006', 'Approved accrual adjustment', 'APPROVED', 2000.00, 2000.00,
       NOW() - INTERVAL '7 days', 'admin', 'MANUAL', 'USD', 'MANUAL', 'flyway', 'flyway'
WHERE NOT EXISTS (SELECT 1 FROM journal_entries je WHERE je.reference_number = 'JE-DEMO-0006');

INSERT INTO journal_entry_lines (journal_entry_id, line_number, account_id, debit, credit, description, created_by, updated_by)
SELECT je.id, 1, acc.id, 2000.00, 0.00, 'Accrued expense recognition', 'flyway', 'flyway'
FROM journal_entries je JOIN accounts acc ON acc.code = '5200'
WHERE je.reference_number = 'JE-DEMO-0006'
  AND NOT EXISTS (SELECT 1 FROM journal_entry_lines l WHERE l.journal_entry_id = je.id AND l.line_number = 1);

INSERT INTO journal_entry_lines (journal_entry_id, line_number, account_id, debit, credit, description, created_by, updated_by)
SELECT je.id, 2, acc.id, 0.00, 2000.00, 'Accrued liabilities', 'flyway', 'flyway'
FROM journal_entries je JOIN accounts acc ON acc.code = '2200'
WHERE je.reference_number = 'JE-DEMO-0006'
  AND NOT EXISTS (SELECT 1 FROM journal_entry_lines l WHERE l.journal_entry_id = je.id AND l.line_number = 2);


-- ---------- source: V40__fix_demo_data_account_codes.sql ----------
SET search_path TO erp_system, public;

-- ============================================================
-- V40: Fix demo data that failed in V39 due to wrong account codes
-- Correct codes: 1210 (AR), 4110 (Sales), 4120 (Service),
--   2110 (AP), 2210 (Tax Payable), 5110 (OpEx), 5120 (Supplies), 5130 (COGS)
-- ============================================================

-- ==================== Customer Invoices ====================

INSERT INTO customer_invoices (
    invoice_number, invoice_date, due_date, customer_name, customer_reference,
    description, subtotal, tax_amount, total_amount, paid_amount, outstanding_amount,
    status, receivable_account_id, revenue_account_id, created_by, updated_by
)
SELECT 'INV-2026-001', CURRENT_DATE - 45, CURRENT_DATE - 15,
       'Global Trading LLC', 'GT-REF-100',
       'Consulting services for Q1 2026', 12000.00, 600.00, 12600.00, 12600.00, 0.00,
       'PAID', ar.id, rev.id, 'flyway', 'flyway'
FROM accounts ar JOIN accounts rev ON rev.code = '4110' WHERE ar.code = '1210'
  AND NOT EXISTS (SELECT 1 FROM customer_invoices ci WHERE ci.invoice_number = 'INV-2026-001');

INSERT INTO customer_invoices (
    invoice_number, invoice_date, due_date, customer_name, customer_reference,
    description, subtotal, tax_amount, total_amount, paid_amount, outstanding_amount,
    status, receivable_account_id, revenue_account_id, created_by, updated_by
)
SELECT 'INV-2026-002', CURRENT_DATE - 30, CURRENT_DATE - 1,
       'Blue Ocean Technologies', 'BOT-2026-Q1',
       'Software development - Phase 1', 25000.00, 1250.00, 26250.00, 15000.00, 11250.00,
       'PARTIAL', ar.id, rev.id, 'flyway', 'flyway'
FROM accounts ar JOIN accounts rev ON rev.code = '4120' WHERE ar.code = '1210'
  AND NOT EXISTS (SELECT 1 FROM customer_invoices ci WHERE ci.invoice_number = 'INV-2026-002');

INSERT INTO customer_invoices (
    invoice_number, invoice_date, due_date, customer_name, customer_reference,
    description, subtotal, tax_amount, total_amount, paid_amount, outstanding_amount,
    status, receivable_account_id, revenue_account_id,
    posted_at, posted_by, created_by, updated_by
)
SELECT 'INV-2026-003', CURRENT_DATE - 15, CURRENT_DATE + 15,
       'Desert Star Enterprises', 'DSE-ADV-07',
       'IT infrastructure setup', 8500.00, 425.00, 8925.00, 0.00, 8925.00,
       'POSTED', ar.id, rev.id, NOW() - INTERVAL '14 days', 'admin', 'flyway', 'flyway'
FROM accounts ar JOIN accounts rev ON rev.code = '4120' WHERE ar.code = '1210'
  AND NOT EXISTS (SELECT 1 FROM customer_invoices ci WHERE ci.invoice_number = 'INV-2026-003');

INSERT INTO customer_invoices (
    invoice_number, invoice_date, due_date, customer_name, customer_reference,
    description, subtotal, tax_amount, total_amount, paid_amount, outstanding_amount,
    status, receivable_account_id, revenue_account_id, created_by, updated_by
)
SELECT 'INV-2026-004', CURRENT_DATE - 5, CURRENT_DATE + 25,
       'Al Manara Group', 'AMG-2026-011',
       'Monthly maintenance contract', 3500.00, 175.00, 3675.00, 0.00, 3675.00,
       'DRAFT', ar.id, rev.id, 'flyway', 'flyway'
FROM accounts ar JOIN accounts rev ON rev.code = '4110' WHERE ar.code = '1210'
  AND NOT EXISTS (SELECT 1 FROM customer_invoices ci WHERE ci.invoice_number = 'INV-2026-004');

INSERT INTO customer_invoices (
    invoice_number, invoice_date, due_date, customer_name, customer_reference,
    description, subtotal, tax_amount, total_amount, paid_amount, outstanding_amount,
    status, receivable_account_id, revenue_account_id,
    cancelled_at, cancelled_by, created_by, updated_by
)
SELECT 'INV-2026-005', CURRENT_DATE - 60, CURRENT_DATE - 30,
       'Phoenix Industries', 'PHX-OLD-009',
       'Cancelled order - equipment supply', 6200.00, 310.00, 6510.00, 0.00, 0.00,
       'CANCELLED', ar.id, rev.id, NOW() - INTERVAL '28 days', 'admin', 'flyway', 'flyway'
FROM accounts ar JOIN accounts rev ON rev.code = '4110' WHERE ar.code = '1210'
  AND NOT EXISTS (SELECT 1 FROM customer_invoices ci WHERE ci.invoice_number = 'INV-2026-005');

-- ==================== Transactions ====================

INSERT INTO transactions (
    transaction_date, reference, description, transaction_type, status, amount,
    debit_account_id, credit_account_id, related_document_reference, created_by, updated_by
)
SELECT CURRENT_DATE - 45, 'TX-DEMO-0003', 'Invoice payment received from Global Trading',
       'SALE', 'POSTED', 12600.00, debit_acc.id, credit_acc.id, 'INV-2026-001', 'flyway', 'flyway'
FROM accounts debit_acc JOIN accounts credit_acc ON credit_acc.code = '4110'
WHERE debit_acc.code = '1120'
  AND NOT EXISTS (SELECT 1 FROM transactions t WHERE t.reference = 'TX-DEMO-0003');

INSERT INTO transactions (
    transaction_date, reference, description, transaction_type, status, amount,
    debit_account_id, credit_account_id, related_document_reference, created_by, updated_by
)
SELECT CURRENT_DATE - 30, 'TX-DEMO-0004', 'Partial payment from Blue Ocean Technologies',
       'SALE', 'POSTED', 15000.00, debit_acc.id, credit_acc.id, 'INV-2026-002', 'flyway', 'flyway'
FROM accounts debit_acc JOIN accounts credit_acc ON credit_acc.code = '4120'
WHERE debit_acc.code = '1120'
  AND NOT EXISTS (SELECT 1 FROM transactions t WHERE t.reference = 'TX-DEMO-0004');

INSERT INTO transactions (
    transaction_date, reference, description, transaction_type, status, amount,
    debit_account_id, credit_account_id, related_document_reference, created_by, updated_by
)
SELECT CURRENT_DATE - 10, 'TX-DEMO-0005', 'Rent payment for office lease',
       'PURCHASE', 'POSTED', 1700.00, debit_acc.id, credit_acc.id, 'PV-DEMO-002', 'flyway', 'flyway'
FROM accounts debit_acc JOIN accounts credit_acc ON credit_acc.code = '1120'
WHERE debit_acc.code = '5110'
  AND NOT EXISTS (SELECT 1 FROM transactions t WHERE t.reference = 'TX-DEMO-0005');

INSERT INTO transactions (
    transaction_date, reference, description, transaction_type, status, amount,
    debit_account_id, credit_account_id, related_document_reference, created_by, updated_by
)
SELECT CURRENT_DATE - 3, 'TX-DEMO-0006', 'Utilities payment - electricity and water',
       'PURCHASE', 'POSTED', 400.00, debit_acc.id, credit_acc.id, 'UTIL-MAR', 'flyway', 'flyway'
FROM accounts debit_acc JOIN accounts credit_acc ON credit_acc.code = '1110'
WHERE debit_acc.code = '5120'
  AND NOT EXISTS (SELECT 1 FROM transactions t WHERE t.reference = 'TX-DEMO-0006');

INSERT INTO transactions (
    transaction_date, reference, description, transaction_type, status, amount,
    debit_account_id, credit_account_id, related_document_reference, created_by, updated_by
)
SELECT CURRENT_DATE - 1, 'TX-DEMO-0007', 'Inventory restocking purchase',
       'PURCHASE', 'DRAFT', 4500.00, debit_acc.id, credit_acc.id, 'PO-2026-015', 'flyway', 'flyway'
FROM accounts debit_acc JOIN accounts credit_acc ON credit_acc.code = '2110'
WHERE debit_acc.code = '5130'
  AND NOT EXISTS (SELECT 1 FROM transactions t WHERE t.reference = 'TX-DEMO-0007');

INSERT INTO transactions (
    transaction_date, reference, description, transaction_type, status, amount,
    debit_account_id, credit_account_id, related_document_reference, created_by, updated_by
)
SELECT CURRENT_DATE, 'TX-DEMO-0008', 'Office supplies purchase',
       'PURCHASE', 'DRAFT', 650.00, debit_acc.id, credit_acc.id, 'BILL-2026-044', 'flyway', 'flyway'
FROM accounts debit_acc JOIN accounts credit_acc ON credit_acc.code = '1120'
WHERE debit_acc.code = '5120'
  AND NOT EXISTS (SELECT 1 FROM transactions t WHERE t.reference = 'TX-DEMO-0008');

INSERT INTO transactions (
    transaction_date, reference, description, transaction_type, status, amount,
    debit_account_id, credit_account_id, related_document_reference, created_by, updated_by
)
SELECT CURRENT_DATE - 20, 'TX-DEMO-0009', 'Client consulting fee - Desert Star',
       'SALE', 'POSTED', 8925.00, debit_acc.id, credit_acc.id, 'INV-2026-003', 'flyway', 'flyway'
FROM accounts debit_acc JOIN accounts credit_acc ON credit_acc.code = '4120'
WHERE debit_acc.code = '1210'
  AND NOT EXISTS (SELECT 1 FROM transactions t WHERE t.reference = 'TX-DEMO-0009');

INSERT INTO transactions (
    transaction_date, reference, description, transaction_type, status, amount,
    debit_account_id, credit_account_id, related_document_reference, created_by, updated_by
)
SELECT CURRENT_DATE - 7, 'TX-DEMO-0010', 'Employee salary payment - March 2026',
       'PURCHASE', 'POSTED', 9500.00, debit_acc.id, credit_acc.id, 'SAL-MAR-2026', 'flyway', 'flyway'
FROM accounts debit_acc JOIN accounts credit_acc ON credit_acc.code = '1120'
WHERE debit_acc.code = '5110'
  AND NOT EXISTS (SELECT 1 FROM transactions t WHERE t.reference = 'TX-DEMO-0010');

-- ==================== Journal Entries ====================

INSERT INTO journal_entries (
    entry_date, reference_number, description, status, total_debit, total_credit,
    source_module, currency_code, entry_type, created_by, updated_by
)
SELECT CURRENT_DATE - 2, 'JE-DEMO-0005', 'Pending inventory adjustment', 'DRAFT', 1500.00, 1500.00,
       'MANUAL', 'USD', 'MANUAL', 'flyway', 'flyway'
WHERE NOT EXISTS (SELECT 1 FROM journal_entries je WHERE je.reference_number = 'JE-DEMO-0005');

INSERT INTO journal_entry_lines (journal_entry_id, line_number, account_id, debit, credit, description, created_by, updated_by)
SELECT je.id, 1, acc.id, 1500.00, 0.00, 'Inventory increase', 'flyway', 'flyway'
FROM journal_entries je JOIN accounts acc ON acc.code = '1210'
WHERE je.reference_number = 'JE-DEMO-0005'
  AND NOT EXISTS (SELECT 1 FROM journal_entry_lines l WHERE l.journal_entry_id = je.id AND l.line_number = 1);

INSERT INTO journal_entry_lines (journal_entry_id, line_number, account_id, debit, credit, description, created_by, updated_by)
SELECT je.id, 2, acc.id, 0.00, 1500.00, 'Accounts payable increase', 'flyway', 'flyway'
FROM journal_entries je JOIN accounts acc ON acc.code = '2110'
WHERE je.reference_number = 'JE-DEMO-0005'
  AND NOT EXISTS (SELECT 1 FROM journal_entry_lines l WHERE l.journal_entry_id = je.id AND l.line_number = 2);

INSERT INTO journal_entries (
    entry_date, reference_number, description, status, total_debit, total_credit,
    posted_at, posted_by, source_module, currency_code, entry_type, created_by, updated_by
)
SELECT CURRENT_DATE - 8, 'JE-DEMO-0006', 'Approved accrual adjustment', 'APPROVED', 2000.00, 2000.00,
       NOW() - INTERVAL '7 days', 'admin', 'MANUAL', 'USD', 'MANUAL', 'flyway', 'flyway'
WHERE NOT EXISTS (SELECT 1 FROM journal_entries je WHERE je.reference_number = 'JE-DEMO-0006');

INSERT INTO journal_entry_lines (journal_entry_id, line_number, account_id, debit, credit, description, created_by, updated_by)
SELECT je.id, 1, acc.id, 2000.00, 0.00, 'Accrued expense recognition', 'flyway', 'flyway'
FROM journal_entries je JOIN accounts acc ON acc.code = '5110'
WHERE je.reference_number = 'JE-DEMO-0006'
  AND NOT EXISTS (SELECT 1 FROM journal_entry_lines l WHERE l.journal_entry_id = je.id AND l.line_number = 1);

INSERT INTO journal_entry_lines (journal_entry_id, line_number, account_id, debit, credit, description, created_by, updated_by)
SELECT je.id, 2, acc.id, 0.00, 2000.00, 'Accrued liabilities', 'flyway', 'flyway'
FROM journal_entries je JOIN accounts acc ON acc.code = '2210'
WHERE je.reference_number = 'JE-DEMO-0006'
  AND NOT EXISTS (SELECT 1 FROM journal_entry_lines l WHERE l.journal_entry_id = je.id AND l.line_number = 2);

INSERT INTO journal_entries (
    entry_date, reference_number, description, status, total_debit, total_credit,
    posted_at, posted_by, source_module, currency_code, entry_type, created_by, updated_by
)
SELECT CURRENT_DATE - 25, 'JE-DEMO-0007', 'Revenue recognition - consulting', 'APPROVED', 8500.00, 8500.00,
       NOW() - INTERVAL '24 days', 'admin', 'MANUAL', 'USD', 'MANUAL', 'flyway', 'flyway'
WHERE NOT EXISTS (SELECT 1 FROM journal_entries je WHERE je.reference_number = 'JE-DEMO-0007');

INSERT INTO journal_entry_lines (journal_entry_id, line_number, account_id, debit, credit, description, created_by, updated_by)
SELECT je.id, 1, acc.id, 8500.00, 0.00, 'Accounts receivable - Desert Star', 'flyway', 'flyway'
FROM journal_entries je JOIN accounts acc ON acc.code = '1210'
WHERE je.reference_number = 'JE-DEMO-0007'
  AND NOT EXISTS (SELECT 1 FROM journal_entry_lines l WHERE l.journal_entry_id = je.id AND l.line_number = 1);

INSERT INTO journal_entry_lines (journal_entry_id, line_number, account_id, debit, credit, description, created_by, updated_by)
SELECT je.id, 2, acc.id, 0.00, 8500.00, 'Service revenue - consulting', 'flyway', 'flyway'
FROM journal_entries je JOIN accounts acc ON acc.code = '4120'
WHERE je.reference_number = 'JE-DEMO-0007'
  AND NOT EXISTS (SELECT 1 FROM journal_entry_lines l WHERE l.journal_entry_id = je.id AND l.line_number = 2);

INSERT INTO journal_entries (
    entry_date, reference_number, description, status, total_debit, total_credit,
    source_module, currency_code, entry_type, created_by, updated_by
)
SELECT CURRENT_DATE - 1, 'JE-DEMO-0008', 'Salary accrual - March 2026', 'DRAFT', 9500.00, 9500.00,
       'MANUAL', 'USD', 'MANUAL', 'flyway', 'flyway'
WHERE NOT EXISTS (SELECT 1 FROM journal_entries je WHERE je.reference_number = 'JE-DEMO-0008');

INSERT INTO journal_entry_lines (journal_entry_id, line_number, account_id, debit, credit, description, created_by, updated_by)
SELECT je.id, 1, acc.id, 9500.00, 0.00, 'Salary expense', 'flyway', 'flyway'
FROM journal_entries je JOIN accounts acc ON acc.code = '5110'
WHERE je.reference_number = 'JE-DEMO-0008'
  AND NOT EXISTS (SELECT 1 FROM journal_entry_lines l WHERE l.journal_entry_id = je.id AND l.line_number = 1);

INSERT INTO journal_entry_lines (journal_entry_id, line_number, account_id, debit, credit, description, created_by, updated_by)
SELECT je.id, 2, acc.id, 0.00, 9500.00, 'Salary payable', 'flyway', 'flyway'
FROM journal_entries je JOIN accounts acc ON acc.code = '2110'
WHERE je.reference_number = 'JE-DEMO-0008'
  AND NOT EXISTS (SELECT 1 FROM journal_entry_lines l WHERE l.journal_entry_id = je.id AND l.line_number = 2);

INSERT INTO journal_entries (
    entry_date, reference_number, description, status, total_debit, total_credit,
    posted_at, posted_by, source_module, currency_code, entry_type, created_by, updated_by
)
SELECT CURRENT_DATE - 40, 'JE-DEMO-0009', 'Opening balance adjustment', 'APPROVED', 50000.00, 50000.00,
       NOW() - INTERVAL '39 days', 'admin', 'MANUAL', 'USD', 'MANUAL', 'flyway', 'flyway'
WHERE NOT EXISTS (SELECT 1 FROM journal_entries je WHERE je.reference_number = 'JE-DEMO-0009');

INSERT INTO journal_entry_lines (journal_entry_id, line_number, account_id, debit, credit, description, created_by, updated_by)
SELECT je.id, 1, acc.id, 50000.00, 0.00, 'Cash opening balance', 'flyway', 'flyway'
FROM journal_entries je JOIN accounts acc ON acc.code = '1120'
WHERE je.reference_number = 'JE-DEMO-0009'
  AND NOT EXISTS (SELECT 1 FROM journal_entry_lines l WHERE l.journal_entry_id = je.id AND l.line_number = 1);

INSERT INTO journal_entry_lines (journal_entry_id, line_number, account_id, debit, credit, description, created_by, updated_by)
SELECT je.id, 2, acc.id, 0.00, 50000.00, 'Owner capital contribution', 'flyway', 'flyway'
FROM journal_entries je JOIN accounts acc ON acc.code = '3110'
WHERE je.reference_number = 'JE-DEMO-0009'
  AND NOT EXISTS (SELECT 1 FROM journal_entry_lines l WHERE l.journal_entry_id = je.id AND l.line_number = 2);

-- ==================== Reconciliation Demo Data ====================

INSERT INTO reconciliations (
    bank_account_id, statement_start_date, statement_end_date,
    opening_balance, closing_balance, system_ending_balance, difference,
    status, created_by, updated_by
)
SELECT ba.id, CURRENT_DATE - 35, CURRENT_DATE - 5,
       45000.00, 48500.00, 48500.00, 0.00,
       'OPEN', 'flyway', 'flyway'
FROM bank_accounts ba
WHERE ba.is_active = true
  AND NOT EXISTS (SELECT 1 FROM reconciliations r WHERE r.bank_account_id = ba.id AND r.statement_end_date = CURRENT_DATE - 5)
LIMIT 1;


-- ---------- source: V41__demo_nested_branch_expense_accounts.sql ----------
SET search_path TO erp_system, public;

-- ============================================================
-- V41: Nested "branch" expense accounts for UI / hierarchy tests
-- Parent accounts are non-posting groups; only leaf accounts are intended for posting.
-- Structure: Expenses â†’ Branch operations â†’ regions â†’ branches â†’ sub-branches (leaf)
-- ============================================================

INSERT INTO accounts (code, name, name_en, name_ar, parent_id, account_type, level, full_path, is_active, opening_balance, opening_balance_side, created_by, updated_by, version)
SELECT '5500', 'Branch operations', 'Branch operations', 'Ø¹Ù…Ù„ÙŠØ§Øª Ø§Ù„ÙØ±ÙˆØ¹', p.id, 'EXPENSE', 2, 'Expenses/Branch operations', TRUE, 0, 'DEBIT', 'flyway', 'flyway', 0
FROM accounts p
WHERE p.code = '5000'
  AND NOT EXISTS (SELECT 1 FROM accounts a WHERE a.code = '5500');

INSERT INTO accounts (code, name, name_en, name_ar, parent_id, account_type, level, full_path, is_active, opening_balance, opening_balance_side, created_by, updated_by, version)
SELECT '5510', 'Northern branches', 'Northern branches', 'ÙØ±ÙˆØ¹ Ø§Ù„Ø´Ù…Ø§Ù„', p.id, 'EXPENSE', 3, 'Expenses/Branch operations/Northern branches', TRUE, 0, 'DEBIT', 'flyway', 'flyway', 0
FROM accounts p
WHERE p.code = '5500'
  AND NOT EXISTS (SELECT 1 FROM accounts a WHERE a.code = '5510');

INSERT INTO accounts (code, name, name_en, name_ar, parent_id, account_type, level, full_path, is_active, opening_balance, opening_balance_side, created_by, updated_by, version)
SELECT '5520', 'Greater Cairo region', 'Greater Cairo region', 'Ù…Ù†Ø·Ù‚Ø© Ø§Ù„Ù‚Ø§Ù‡Ø±Ø© Ø§Ù„ÙƒØ¨Ø±Ù‰', p.id, 'EXPENSE', 3, 'Expenses/Branch operations/Greater Cairo region', TRUE, 0, 'DEBIT', 'flyway', 'flyway', 0
FROM accounts p
WHERE p.code = '5500'
  AND NOT EXISTS (SELECT 1 FROM accounts a WHERE a.code = '5520');

INSERT INTO accounts (code, name, name_en, name_ar, parent_id, account_type, level, full_path, is_active, opening_balance, opening_balance_side, created_by, updated_by, version)
SELECT '5511', 'Alexandria branch', 'Alexandria branch', 'ÙØ±Ø¹ Ø§Ù„Ø¥Ø³ÙƒÙ†Ø¯Ø±ÙŠØ©', p.id, 'EXPENSE', 4, 'Expenses/Branch operations/Northern branches/Alexandria branch', TRUE, 0, 'DEBIT', 'flyway', 'flyway', 0
FROM accounts p
WHERE p.code = '5510'
  AND NOT EXISTS (SELECT 1 FROM accounts a WHERE a.code = '5511');

INSERT INTO accounts (code, name, name_en, name_ar, parent_id, account_type, level, full_path, is_active, opening_balance, opening_balance_side, created_by, updated_by, version)
SELECT '5512', 'North Coast kiosk', 'North Coast kiosk', 'ÙƒØ´Ùƒ Ø§Ù„Ø³Ø§Ø­Ù„ Ø§Ù„Ø´Ù…Ø§Ù„ÙŠ', p.id, 'EXPENSE', 4, 'Expenses/Branch operations/Northern branches/North Coast kiosk', TRUE, 0, 'DEBIT', 'flyway', 'flyway', 0
FROM accounts p
WHERE p.code = '5510'
  AND NOT EXISTS (SELECT 1 FROM accounts a WHERE a.code = '5512');

INSERT INTO accounts (code, name, name_en, name_ar, parent_id, account_type, level, full_path, is_active, opening_balance, opening_balance_side, created_by, updated_by, version)
SELECT '5521', 'Cairo HQ', 'Cairo HQ', 'Ù…Ù‚Ø± Ø§Ù„Ù‚Ø§Ù‡Ø±Ø©', p.id, 'EXPENSE', 4, 'Expenses/Branch operations/Greater Cairo region/Cairo HQ', TRUE, 0, 'DEBIT', 'flyway', 'flyway', 0
FROM accounts p
WHERE p.code = '5520'
  AND NOT EXISTS (SELECT 1 FROM accounts a WHERE a.code = '5521');

-- Branch under a branch (Giza nested under Greater Cairo)
INSERT INTO accounts (code, name, name_en, name_ar, parent_id, account_type, level, full_path, is_active, opening_balance, opening_balance_side, created_by, updated_by, version)
SELECT '5530', 'Giza branch', 'Giza branch', 'ÙØ±Ø¹ Ø§Ù„Ø¬ÙŠØ²Ø©', p.id, 'EXPENSE', 4, 'Expenses/Branch operations/Greater Cairo region/Giza branch', TRUE, 0, 'DEBIT', 'flyway', 'flyway', 0
FROM accounts p
WHERE p.code = '5520'
  AND NOT EXISTS (SELECT 1 FROM accounts a WHERE a.code = '5530');

INSERT INTO accounts (code, name, name_en, name_ar, parent_id, account_type, level, full_path, is_active, opening_balance, opening_balance_side, created_by, updated_by, version)
SELECT '5531', 'Giza warehouse', 'Giza warehouse', 'Ù…Ø®Ø²Ù† Ø§Ù„Ø¬ÙŠØ²Ø©', p.id, 'EXPENSE', 5, 'Expenses/Branch operations/Greater Cairo region/Giza branch/Giza warehouse', TRUE, 0, 'DEBIT', 'flyway', 'flyway', 0
FROM accounts p
WHERE p.code = '5530'
  AND NOT EXISTS (SELECT 1 FROM accounts a WHERE a.code = '5531');

INSERT INTO accounts (code, name, name_en, name_ar, parent_id, account_type, level, full_path, is_active, opening_balance, opening_balance_side, created_by, updated_by, version)
SELECT '5532', 'Giza retail', 'Giza retail', 'ØªØ¬Ø²Ø¦Ø© Ø§Ù„Ø¬ÙŠØ²Ø©', p.id, 'EXPENSE', 5, 'Expenses/Branch operations/Greater Cairo region/Giza branch/Giza retail', TRUE, 0, 'DEBIT', 'flyway', 'flyway', 0
FROM accounts p
WHERE p.code = '5530'
  AND NOT EXISTS (SELECT 1 FROM accounts a WHERE a.code = '5532');


-- ---------- source: V42__asset_banks_bank_muscat.sql ----------
SET search_path TO erp_system, public;

-- ============================================================
-- V42: Asset hierarchy â€” Banks (Ø¨Ù†ÙˆÙƒ) under Assets, Bank Muscat (Ø¨Ù†Ùƒ Ù…Ø³Ù‚Ø·) leaf + demo bank row
-- ============================================================

INSERT INTO accounts (code, name, name_en, name_ar, parent_id, account_type, level, full_path, is_active, opening_balance, opening_balance_side, created_by, updated_by, version)
SELECT '1140', 'Banks', 'Banks', 'Ø¨Ù†ÙˆÙƒ', p.id, 'ASSET', 2, 'Assets/Banks', TRUE, 0, 'DEBIT', 'flyway', 'flyway', 0
FROM accounts p
WHERE p.code = '1000'
  AND NOT EXISTS (SELECT 1 FROM accounts a WHERE a.code = '1140');

INSERT INTO accounts (code, name, name_en, name_ar, parent_id, account_type, level, full_path, is_active, opening_balance, opening_balance_side, created_by, updated_by, version)
SELECT '1141', 'Bank Muscat', 'Bank Muscat', 'Ø¨Ù†Ùƒ Ù…Ø³Ù‚Ø·', p.id, 'ASSET', 3, 'Assets/Banks/Bank Muscat', TRUE, 8750.50, 'DEBIT', 'flyway', 'flyway', 0
FROM accounts p
WHERE p.code = '1140'
  AND NOT EXISTS (SELECT 1 FROM accounts a WHERE a.code = '1141');

INSERT INTO bank_accounts (bank_name, account_number, iban, currency, opening_balance, current_balance, linked_account_id, is_active, created_by, updated_by)
SELECT 'Bank Muscat', 'MCT-OM-001234567', 'OM120000000000987654321', 'OMR', 8750.50, 8750.50, acc.id, TRUE, 'flyway', 'flyway'
FROM accounts acc
WHERE acc.code = '1141'
  AND NOT EXISTS (SELECT 1 FROM bank_accounts b WHERE b.linked_account_id = acc.id);


-- ---------- source: V43__arabic_labels_demo_and_lookups.sql ----------
-- V43: Arabic display labels for lookups and seeded demo/user-facing text.
-- Fills or corrects name_ar on lookup_values (often left NULL after V24 INITCAP name_en),
-- and translates Flyway demo rows so Arabic UI shows proper copy.
SET search_path TO erp_system, public;

-- ---------------------------------------------------------------------------
-- lookup_values: bilingual labels (English kept readable; Arabic for UI)
-- ---------------------------------------------------------------------------
UPDATE lookup_values lv
SET name_en = v.name_en,
    name_ar = v.name_ar,
    updated_by = 'flyway',
    updated_at = NOW()
FROM (VALUES
          -- account-types (aligned with V37 bilingual labels)
          ('account-types', 'ASSET', 'Asset', 'Ø§Ù„Ø£ØµÙˆÙ„'),
          ('account-types', 'LIABILITY', 'Liability', 'Ø§Ù„Ø§Ù„ØªØ²Ø§Ù…Ø§Øª'),
          ('account-types', 'EQUITY', 'Equity', 'Ø­Ù‚ÙˆÙ‚ Ø§Ù„Ù…Ù„ÙƒÙŠØ©'),
          ('account-types', 'INCOME', 'Income', 'Ø§Ù„Ø¥ÙŠØ±Ø§Ø¯Ø§Øª'),
          ('account-types', 'REVENUE', 'Revenue', 'Ø§Ù„Ø¥ÙŠØ±Ø§Ø¯Ø§Øª'),
          ('account-types', 'EXPENSE', 'Expense', 'Ø§Ù„Ù…ØµØ±ÙˆÙØ§Øª'),
          -- voucher-statuses
          ('voucher-statuses', 'DRAFT', 'Draft', 'Ù…Ø³ÙˆØ¯Ø©'),
          ('voucher-statuses', 'APPROVED', 'Approved', 'Ù…Ø¹ØªÙ…Ø¯'),
          ('voucher-statuses', 'CANCELLED', 'Cancelled', 'Ù…Ù„ØºÙ‰'),
          -- voucher-types
          ('voucher-types', 'STANDARD', 'Standard', 'Ù‚ÙŠØ§Ø³ÙŠ'),
          ('voucher-types', 'ADVANCE', 'Advance', 'Ø³Ù„ÙØ©'),
          ('voucher-types', 'BILL_PAYMENT', 'Bill payment', 'Ø¯ÙØ¹ ÙØ§ØªÙˆØ±Ø©'),
          ('voucher-types', 'INVOICE_COLLECTION', 'Invoice collection', 'ØªØ­ØµÙŠÙ„ ÙØ§ØªÙˆØ±Ø©'),
          -- payment-methods
          ('payment-methods', 'CASH', 'Cash', 'Ù†Ù‚Ø¯ÙŠ'),
          ('payment-methods', 'BANK', 'Bank', 'Ø¨Ù†Ùƒ'),
          ('payment-methods', 'CHECK', 'Cheque', 'Ø´ÙŠÙƒ'),
          -- currencies (codes stay Latin; labels Arabic)
          ('currencies', 'USD', 'US Dollar', 'Ø¯ÙˆÙ„Ø§Ø± Ø£Ù…Ø±ÙŠÙƒÙŠ'),
          ('currencies', 'EUR', 'Euro', 'ÙŠÙˆØ±Ùˆ'),
          ('currencies', 'GBP', 'British Pound', 'Ø¬Ù†ÙŠÙ‡ Ø¥Ø³ØªØ±Ù„ÙŠÙ†ÙŠ'),
          ('currencies', 'AED', 'UAE Dirham', 'Ø¯Ø±Ù‡Ù… Ø¥Ù…Ø§Ø±Ø§ØªÙŠ'),
          ('currencies', 'SAR', 'Saudi Riyal', 'Ø±ÙŠØ§Ù„ Ø³Ø¹ÙˆØ¯ÙŠ'),
          ('currencies', 'EGP', 'Egyptian Pound', 'Ø¬Ù†ÙŠÙ‡ Ù…ØµØ±ÙŠ'),
          ('currencies', 'OMR', 'Omani Rial', 'Ø±ÙŠØ§Ù„ Ø¹Ù…Ø§Ù†ÙŠ'),
          -- reconciliation
          ('reconciliation-statuses', 'OPEN', 'Open', 'Ù…ÙØªÙˆØ­Ø©'),
          ('reconciliation-statuses', 'IN_PROGRESS', 'In progress', 'Ù‚ÙŠØ¯ Ø§Ù„ØªÙ†ÙÙŠØ°'),
          ('reconciliation-statuses', 'COMPLETED', 'Completed', 'Ù…ÙƒØªÙ…Ù„Ø©'),
          ('reconciliation-line-statuses', 'UNMATCHED', 'Unmatched', 'ØºÙŠØ± Ù…Ø·Ø§Ø¨Ù‚'),
          ('reconciliation-line-statuses', 'PARTIALLY_MATCHED', 'Partially matched', 'Ù…Ø·Ø§Ø¨Ù‚ Ø¬Ø²Ø¦ÙŠØ§Ù‹'),
          ('reconciliation-line-statuses', 'MATCHED', 'Matched', 'Ù…Ø·Ø§Ø¨Ù‚'),
          -- report-periods
          ('report-periods', 'THIS_MONTH', 'This month', 'Ù‡Ø°Ø§ Ø§Ù„Ø´Ù‡Ø±'),
          ('report-periods', 'LAST_MONTH', 'Last month', 'Ø§Ù„Ø´Ù‡Ø± Ø§Ù„Ù…Ø§Ø¶ÙŠ'),
          ('report-periods', 'THIS_QUARTER', 'This quarter', 'Ù‡Ø°Ø§ Ø§Ù„Ø±Ø¨Ø¹'),
          ('report-periods', 'THIS_YEAR', 'This year', 'Ù‡Ø°Ù‡ Ø§Ù„Ø³Ù†Ø©'),
          ('report-periods', 'CUSTOM', 'Custom', 'Ù…Ø®ØµØµ'),
          -- journal-entry-statuses
          ('journal-entry-statuses', 'DRAFT', 'Draft', 'Ù…Ø³ÙˆØ¯Ø©'),
          ('journal-entry-statuses', 'POSTED', 'Posted', 'Ù…Ø±Ø­Ù‘Ù„'),
          ('journal-entry-statuses', 'APPROVED', 'Approved', 'Ù…Ø¹ØªÙ…Ø¯'),
          ('journal-entry-statuses', 'REVERSED', 'Reversed', 'Ù…Ø¹ÙƒÙˆØ³'),
          ('journal-entry-statuses', 'CANCELLED', 'Cancelled', 'Ù…Ù„ØºÙ‰'),
          -- entry-types
          ('entry-types', 'MANUAL', 'Manual', 'ÙŠØ¯ÙˆÙŠ'),
          ('entry-types', 'ADJUSTMENT', 'Adjustment', 'ØªØ³ÙˆÙŠØ©'),
          ('entry-types', 'OPENING', 'Opening', 'Ø§ÙØªØªØ§Ø­ÙŠ'),
          ('entry-types', 'CLOSING', 'Closing', 'Ø¥ØºÙ„Ø§Ù‚'),
          ('entry-types', 'REVERSAL', 'Reversal', 'Ø¹ÙƒØ³ Ù‚ÙŠØ¯'),
          -- statuses
          ('statuses', 'ACTIVE', 'Active', 'Ù†Ø´Ø·'),
          ('statuses', 'INACTIVE', 'Inactive', 'ØºÙŠØ± Ù†Ø´Ø·'),
          -- accounting-methods
          ('accounting-methods', 'ACCRUAL', 'Accrual basis', 'Ø¹Ù„Ù‰ Ø£Ø³Ø§Ø³ Ø§Ù„Ø§Ø³ØªØ­Ù‚Ø§Ù‚'),
          ('accounting-methods', 'CASH', 'Cash basis', 'Ø¹Ù„Ù‰ Ø£Ø³Ø§Ø³ Ø§Ù„Ù†Ù‚Ø¯ÙŠØ©'),
          -- transaction-types
          ('transaction-types', 'SALE', 'Sale', 'Ø¨ÙŠØ¹'),
          ('transaction-types', 'PURCHASE', 'Purchase', 'Ø´Ø±Ø§Ø¡'),
          ('transaction-types', 'REFUND', 'Refund', 'Ø§Ø³ØªØ±Ø¯Ø§Ø¯'),
          ('transaction-types', 'ADJUSTMENT', 'Adjustment', 'ØªØ³ÙˆÙŠØ©'),
          -- transaction-statuses
          ('transaction-statuses', 'DRAFT', 'Draft', 'Ù…Ø³ÙˆØ¯Ø©'),
          ('transaction-statuses', 'POSTED', 'Posted', 'Ù…Ø±Ø­Ù‘Ù„'),
          ('transaction-statuses', 'PENDING', 'Pending', 'Ù‚ÙŠØ¯ Ø§Ù„Ø§Ù†ØªØ¸Ø§Ø±'),
          ('transaction-statuses', 'COMPLETED', 'Completed', 'Ù…ÙƒØªÙ…Ù„'),
          ('transaction-statuses', 'CANCELLED', 'Cancelled', 'Ù…Ù„ØºÙ‰'),
          -- bill-statuses
          ('bill-statuses', 'DRAFT', 'Draft', 'Ù…Ø³ÙˆØ¯Ø©'),
          ('bill-statuses', 'APPROVED', 'Approved', 'Ù…Ø¹ØªÙ…Ø¯'),
          ('bill-statuses', 'POSTED', 'Posted', 'Ù…Ø±Ø­Ù‘Ù„'),
          ('bill-statuses', 'PARTIALLY_PAID', 'Partially paid', 'Ù…Ø¯ÙÙˆØ¹ Ø¬Ø²Ø¦ÙŠØ§Ù‹'),
          ('bill-statuses', 'PAID', 'Paid', 'Ù…Ø¯ÙÙˆØ¹ Ø¨Ø§Ù„ÙƒØ§Ù…Ù„'),
          ('bill-statuses', 'CANCELLED', 'Cancelled', 'Ù…Ù„ØºÙ‰'),
          -- budget-statuses
          ('budget-statuses', 'DRAFT', 'Draft', 'Ù…Ø³ÙˆØ¯Ø©'),
          ('budget-statuses', 'APPROVED', 'Approved', 'Ù…Ø¹ØªÙ…Ø¯'),
          ('budget-statuses', 'ACTIVE', 'Active', 'Ù†Ø´Ø·'),
          ('budget-statuses', 'CLOSED', 'Closed', 'Ù…ØºÙ„Ù‚'),
          -- check-types
          ('check-types', 'ISSUED', 'Issued', 'ØµØ§Ø¯Ø±'),
          ('check-types', 'RECEIVED', 'Received', 'ÙˆØ§Ø±Ø¯'),
          -- check-statuses
          ('check-statuses', 'PENDING', 'Pending', 'Ù‚ÙŠØ¯ Ø§Ù„Ø§Ù†ØªØ¸Ø§Ø±'),
          ('check-statuses', 'DEPOSITED', 'Deposited', 'Ù…ÙˆØ¯Ø¹'),
          ('check-statuses', 'CLEARED', 'Cleared', 'Ù…Ø³Ø¯Ø¯'),
          ('check-statuses', 'BOUNCED', 'Bounced', 'Ù…Ø±ØªØ¬Ø¹'),
          ('check-statuses', 'CANCELLED', 'Cancelled', 'Ù…Ù„ØºÙ‰')
      ) AS v(type_code, code, name_en, name_ar)
WHERE lv.type_code = v.type_code
  AND lv.code = v.code;

-- ---------------------------------------------------------------------------
-- Demo users: Arabic names for profile display
-- ---------------------------------------------------------------------------
UPDATE user_profiles p
SET full_name = v.full_name,
    company_name = COALESCE(v.company_name, p.company_name),
    updated_by = 'flyway',
    updated_at = NOW()
FROM users u,
     (VALUES
          ('chief.accountant', 'Ø§Ù„Ù…Ø­Ø§Ø³Ø¨ Ø§Ù„Ø±Ø¦ÙŠØ³ÙŠ', 'Ø´Ø±ÙƒØ© ØªØ¬Ø±ÙŠØ¨ÙŠØ© Ù„Ù„Ù†Ø¸Ø§Ù…'),
          ('treasury.user', 'Ù…Ø³Ø¤ÙˆÙ„ Ø§Ù„Ø®Ø²ÙŠÙ†Ø©', 'Ø´Ø±ÙƒØ© ØªØ¬Ø±ÙŠØ¨ÙŠØ© Ù„Ù„Ù†Ø¸Ø§Ù…'),
          ('report.viewer', 'Ù…Ø³ØªØ¹Ø±Ø¶ Ø§Ù„ØªÙ‚Ø§Ø±ÙŠØ±', 'Ø´Ø±ÙƒØ© ØªØ¬Ø±ÙŠØ¨ÙŠØ© Ù„Ù„Ù†Ø¸Ø§Ù…'),
          ('finance.manager', 'Ù…Ø¯ÙŠØ± Ø§Ù„Ø´Ø¤ÙˆÙ† Ø§Ù„Ù…Ø§Ù„ÙŠØ©', 'Ø´Ø±ÙƒØ© ØªØ¬Ø±ÙŠØ¨ÙŠØ© Ù„Ù„Ù†Ø¸Ø§Ù…')
      ) AS v(username, full_name, company_name)
WHERE p.user_id = u.id
  AND u.username = v.username;

-- ---------------------------------------------------------------------------
-- Customer invoices (demo)
-- ---------------------------------------------------------------------------
UPDATE customer_invoices
SET customer_name = v.customer_name,
    description = v.description,
    updated_by = 'flyway',
    updated_at = NOW()
FROM (VALUES
          ('INV-2026-001', 'Ø´Ø±ÙƒØ© Ø§Ù„ØªØ¬Ø§Ø±Ø© Ø§Ù„Ø¹Ø§Ù„Ù…ÙŠØ© Ø°.Ù….Ù….', 'Ø®Ø¯Ù…Ø§Øª Ø§Ø³ØªØ´Ø§Ø±ÙŠØ© Ù„Ù„Ø±Ø¨Ø¹ Ø§Ù„Ø£ÙˆÙ„ 2026'),
          ('INV-2026-002', 'ØªÙ‚Ù†ÙŠØ§Øª Ø§Ù„Ù…Ø­ÙŠØ· Ø§Ù„Ø£Ø²Ø±Ù‚', 'ØªØ·ÙˆÙŠØ± Ø¨Ø±Ù…Ø¬ÙŠØ§Øª - Ø§Ù„Ù…Ø±Ø­Ù„Ø© Ø§Ù„Ø£ÙˆÙ„Ù‰'),
          ('INV-2026-003', 'Ù…Ø¤Ø³Ø³Ø© Ù†Ø¬Ù… Ø§Ù„ØµØ­Ø±Ø§Ø¡', 'ØªØ¬Ù‡ÙŠØ² Ø¨Ù†ÙŠØ© ØªÙ‚Ù†ÙŠØ© Ø§Ù„Ù…Ø¹Ù„ÙˆÙ…Ø§Øª'),
          ('INV-2026-004', 'Ù…Ø¬Ù…ÙˆØ¹Ø© Ø§Ù„Ù…Ù†Ø§Ø±Ø©', 'Ø¹Ù‚Ø¯ ØµÙŠØ§Ù†Ø© Ø´Ù‡Ø±ÙŠ'),
          ('INV-2026-005', 'ØµÙ†Ø§Ø¹Ø§Øª Ø§Ù„Ø¹Ù†Ù‚Ø§Ø¡', 'Ø·Ù„Ø¨ Ù…Ù„ØºÙ‰ - ØªÙˆØ±ÙŠØ¯ Ù…Ø¹Ø¯Ø§Øª')
      ) AS v(invoice_number, customer_name, description)
WHERE customer_invoices.invoice_number = v.invoice_number;

UPDATE customer_invoices
SET customer_name = 'Ø¹Ù…ÙŠÙ„ ØªØ¬Ø±ÙŠØ¨ÙŠ',
    description = 'ÙØ§ØªÙˆØ±Ø© Ø£ÙˆÙ„ÙŠØ© Ù„Ù„Ø§Ø®ØªØ¨Ø§Ø±',
    updated_by = 'flyway',
    updated_at = NOW()
WHERE invoice_number = 'INV-000001';

UPDATE customer_invoice_lines
SET description = 'Ø³Ø·Ø± ÙØ§ØªÙˆØ±Ø© ØªØ¬Ø±ÙŠØ¨ÙŠ',
    updated_by = 'flyway',
    updated_at = NOW()
WHERE description = 'Seeded invoice line';

-- ---------------------------------------------------------------------------
-- Transactions (demo)
-- ---------------------------------------------------------------------------
UPDATE transactions
SET description = v.description,
    updated_by = 'flyway',
    updated_at = NOW()
FROM (VALUES
          ('TX-DEMO-0001', 'Ù…Ø¹Ø§Ù…Ù„Ø© Ø¥ÙŠØ¯Ø§Ø¹ Ø¨Ù†ÙƒÙŠ'),
          ('TX-DEMO-0002', 'Ù…Ø¹Ø§Ù…Ù„Ø© Ø¯ÙØ¹ Ø±ÙˆØ§ØªØ¨'),
          ('TX-DEMO-0003', 'ØªØ­ØµÙŠÙ„ Ø¯ÙØ¹Ø© ÙØ§ØªÙˆØ±Ø© Ù…Ù† Ø´Ø±ÙƒØ© Ø§Ù„ØªØ¬Ø§Ø±Ø© Ø§Ù„Ø¹Ø§Ù„Ù…ÙŠØ©'),
          ('TX-DEMO-0004', 'Ø¯ÙØ¹Ø© Ø¬Ø²Ø¦ÙŠØ© Ù…Ù† ØªÙ‚Ù†ÙŠØ§Øª Ø§Ù„Ù…Ø­ÙŠØ· Ø§Ù„Ø£Ø²Ø±Ù‚'),
          ('TX-DEMO-0005', 'Ø¯ÙØ¹ Ø¥ÙŠØ¬Ø§Ø± Ø§Ù„Ù…ÙƒØªØ¨'),
          ('TX-DEMO-0006', 'Ø¯ÙØ¹ ÙÙˆØ§ØªÙŠØ± Ø§Ù„ÙƒÙ‡Ø±Ø¨Ø§Ø¡ ÙˆØ§Ù„Ù…Ø§Ø¡'),
          ('TX-DEMO-0007', 'Ø´Ø±Ø§Ø¡ Ù„Ø¥Ø¹Ø§Ø¯Ø© ØªØ®Ø²ÙŠÙ† Ø§Ù„Ù…Ø®Ø²ÙˆÙ†'),
          ('TX-DEMO-0008', 'Ø´Ø±Ø§Ø¡ Ù…Ø³ØªÙ„Ø²Ù…Ø§Øª Ù…ÙƒØªØ¨ÙŠØ©'),
          ('TX-DEMO-0009', 'Ø£ØªØ¹Ø§Ø¨ Ø§Ø³ØªØ´Ø§Ø±ÙŠØ© - Ø¹Ù…ÙŠÙ„ Ù†Ø¬Ù… Ø§Ù„ØµØ­Ø±Ø§Ø¡'),
          ('TX-DEMO-0010', 'Ø¯ÙØ¹ Ø±ÙˆØ§ØªØ¨ Ø§Ù„Ù…ÙˆØ¸ÙÙŠÙ† - Ù…Ø§Ø±Ø³ 2026')
      ) AS v(reference, description)
WHERE transactions.reference = v.reference;

-- ---------------------------------------------------------------------------
-- Journal entries & lines (demo)
-- ---------------------------------------------------------------------------
UPDATE journal_entries
SET description = v.description,
    updated_by = 'flyway',
    updated_at = NOW()
FROM (VALUES
          ('JE-DEMO-0005', 'ØªØ³ÙˆÙŠØ© Ù…Ø®Ø²ÙˆÙ† Ù…Ø¹Ù„Ù‚Ø©'),
          ('JE-DEMO-0006', 'ØªØ³ÙˆÙŠØ© Ù…Ø³ØªØ­Ù‚Ø§Øª Ù…Ø¹ØªÙ…Ø¯Ø©'),
          ('JE-DEMO-0007', 'Ø¥Ø«Ø¨Ø§Øª Ø¥ÙŠØ±Ø§Ø¯Ø§Øª - Ø§Ø³ØªØ´Ø§Ø±Ø§Øª'),
          ('JE-DEMO-0008', 'Ø§Ø³ØªØ­Ù‚Ø§Ù‚ Ø±ÙˆØ§ØªØ¨ - Ù…Ø§Ø±Ø³ 2026'),
          ('JE-DEMO-0009', 'ØªØ³ÙˆÙŠØ© Ø§Ù„Ø±ØµÙŠØ¯ Ø§Ù„Ø§ÙØªØªØ§Ø­ÙŠ')
      ) AS v(reference_number, description)
WHERE journal_entries.reference_number = v.reference_number;

UPDATE journal_entry_lines jel
SET description = 'Ø²ÙŠØ§Ø¯Ø© Ø§Ù„Ù…Ø®Ø²ÙˆÙ†',
    updated_by = 'flyway',
    updated_at = NOW()
FROM journal_entries je
WHERE je.id = jel.journal_entry_id
  AND je.reference_number = 'JE-DEMO-0005'
  AND jel.line_number = 1
  AND jel.description = 'Inventory increase';

UPDATE journal_entry_lines jel
SET description = 'Ø²ÙŠØ§Ø¯Ø© Ø§Ù„Ø°Ù…Ù… Ø§Ù„Ø¯Ø§Ø¦Ù†Ø©',
    updated_by = 'flyway',
    updated_at = NOW()
FROM journal_entries je
WHERE je.id = jel.journal_entry_id
  AND je.reference_number = 'JE-DEMO-0005'
  AND jel.line_number = 2
  AND jel.description = 'Accounts payable increase';

UPDATE journal_entry_lines jel
SET description = 'Ø¥Ø«Ø¨Ø§Øª Ù…ØµØ±ÙˆÙ Ù…Ø³ØªØ­Ù‚',
    updated_by = 'flyway',
    updated_at = NOW()
FROM journal_entries je
WHERE je.id = jel.journal_entry_id
  AND je.reference_number = 'JE-DEMO-0006'
  AND jel.line_number = 1
  AND jel.description = 'Accrued expense recognition';

UPDATE journal_entry_lines jel
SET description = 'Ø§Ù„ØªØ²Ø§Ù…Ø§Øª Ù…Ø³ØªØ­Ù‚Ø©',
    updated_by = 'flyway',
    updated_at = NOW()
FROM journal_entries je
WHERE je.id = jel.journal_entry_id
  AND je.reference_number = 'JE-DEMO-0006'
  AND jel.line_number = 2
  AND jel.description = 'Accrued liabilities';

UPDATE journal_entry_lines jel
SET description = 'Ø°Ù…Ù… Ù…Ø¯ÙŠÙ†Ø© - Ù†Ø¬Ù… Ø§Ù„ØµØ­Ø±Ø§Ø¡',
    updated_by = 'flyway',
    updated_at = NOW()
FROM journal_entries je
WHERE je.id = jel.journal_entry_id
  AND je.reference_number = 'JE-DEMO-0007'
  AND jel.line_number = 1
  AND jel.description = 'Accounts receivable - Desert Star';

UPDATE journal_entry_lines jel
SET description = 'Ø¥ÙŠØ±Ø§Ø¯ Ø®Ø¯Ù…Ø§Øª - Ø§Ø³ØªØ´Ø§Ø±Ø§Øª',
    updated_by = 'flyway',
    updated_at = NOW()
FROM journal_entries je
WHERE je.id = jel.journal_entry_id
  AND je.reference_number = 'JE-DEMO-0007'
  AND jel.line_number = 2
  AND jel.description = 'Service revenue - consulting';

UPDATE journal_entry_lines jel
SET description = 'Ù…ØµØ±ÙˆÙ Ø±ÙˆØ§ØªØ¨',
    updated_by = 'flyway',
    updated_at = NOW()
FROM journal_entries je
WHERE je.id = jel.journal_entry_id
  AND je.reference_number = 'JE-DEMO-0008'
  AND jel.line_number = 1
  AND jel.description = 'Salary expense';

UPDATE journal_entry_lines jel
SET description = 'Ø±ÙˆØ§ØªØ¨ Ù…Ø³ØªØ­Ù‚Ø© Ø§Ù„Ø¯ÙØ¹',
    updated_by = 'flyway',
    updated_at = NOW()
FROM journal_entries je
WHERE je.id = jel.journal_entry_id
  AND je.reference_number = 'JE-DEMO-0008'
  AND jel.line_number = 2
  AND jel.description = 'Salary payable';

UPDATE journal_entry_lines jel
SET description = 'Ø±ØµÙŠØ¯ Ù†Ù‚Ø¯ÙŠØ© Ø§ÙØªØªØ§Ø­ÙŠ',
    updated_by = 'flyway',
    updated_at = NOW()
FROM journal_entries je
WHERE je.id = jel.journal_entry_id
  AND je.reference_number = 'JE-DEMO-0009'
  AND jel.line_number = 1
  AND jel.description = 'Cash opening balance';

UPDATE journal_entry_lines jel
SET description = 'Ù…Ø³Ø§Ù‡Ù…Ø© Ø±Ø£Ø³ Ù…Ø§Ù„ Ø§Ù„Ù…Ø§Ù„Ùƒ',
    updated_by = 'flyway',
    updated_at = NOW()
FROM journal_entries je
WHERE je.id = jel.journal_entry_id
  AND je.reference_number = 'JE-DEMO-0009'
  AND jel.line_number = 2
  AND jel.description = 'Owner capital contribution';

-- V17 legacy journal descriptions (if still present)
UPDATE journal_entries
SET description = 'ØªØ±Ø­ÙŠÙ„ ÙØ§ØªÙˆØ±Ø© Ø¨ÙŠØ¹ ØªØ¬Ø±ÙŠØ¨ÙŠØ©',
    updated_by = 'flyway',
    updated_at = NOW()
WHERE reference_number = 'JE-DEMO-0001' AND description = 'Demo sale invoice posting';

UPDATE journal_entries
SET description = 'Ø¯ÙØ¹ Ø±Ø§ØªØ¨ ØªØ¬Ø±ÙŠØ¨ÙŠ',
    updated_by = 'flyway',
    updated_at = NOW()
WHERE reference_number = 'JE-DEMO-0002' AND description = 'Demo salary payment';

UPDATE journal_entries
SET description = 'Ø¥ÙŠØ¬Ø§Ø± ÙˆÙ…Ø±Ø§ÙÙ‚ ØªØ¬Ø±ÙŠØ¨ÙŠØ©',
    updated_by = 'flyway',
    updated_at = NOW()
WHERE reference_number = 'JE-DEMO-0003' AND description = 'Demo rent and utilities';

UPDATE journal_entries
SET description = 'Ø¥ÙŠØµØ§Ù„ Ø®Ø¯Ù…Ø© ØªØ¬Ø±ÙŠØ¨ÙŠ',
    updated_by = 'flyway',
    updated_at = NOW()
WHERE reference_number = 'JE-DEMO-0004' AND description = 'Demo service receipt';

-- ---------------------------------------------------------------------------
-- Checks (demo): Arabic bank / party labels
-- ---------------------------------------------------------------------------
UPDATE checks
SET bank_name = v.bank_name,
    party_name = v.party_name,
    updated_by = 'flyway',
    updated_at = NOW()
FROM (VALUES
          ('CHK-ISS-001', 'Ø¨Ù†Ùƒ Ø§Ù„Ø¥Ù…Ø§Ø±Ø§Øª Ø¯Ø¨ÙŠ Ø§Ù„ÙˆØ·Ù†ÙŠ', 'Ø±ÙˆØ§ØªØ¨ Ø§Ù„ÙØ±ÙŠÙ‚'),
          ('CHK-ISS-002', 'Ø¨Ù†Ùƒ Ø§Ù„Ø¥Ù…Ø§Ø±Ø§Øª Ø¯Ø¨ÙŠ Ø§Ù„ÙˆØ·Ù†ÙŠ', 'Ø§Ù„Ø³Ù„Ø§Ù… Ø§Ù„Ø¹Ù‚Ø§Ø±ÙŠØ©'),
          ('CHK-ISS-003', 'Ù…ØµØ±Ù Ø£Ø¨ÙˆØ¸Ø¨ÙŠ Ø§Ù„Ø¥Ø³Ù„Ø§Ù…ÙŠ', 'Ø£ÙˆÙÙŠØ³ Ù…Ø§Ø±Øª Ø°.Ù….Ù….'),
          ('CHK-RCV-001', 'Ø¨Ù†Ùƒ Ø£Ø¨ÙˆØ¸Ø¨ÙŠ Ø§Ù„ÙˆØ·Ù†ÙŠ', 'Ø¹Ù…ÙŠÙ„ Ø®Ø¯Ù…Ø§Øª Ø£ÙƒÙ…ÙŠ'),
          ('CHK-RCV-002', 'Ù…ØµØ±Ù Ø¯Ø¨ÙŠ Ø§Ù„Ø¥Ø³Ù„Ø§Ù…ÙŠ', 'Ø§Ù„Ù…Ø­ÙŠØ· Ø§Ù„Ø£Ø²Ø±Ù‚ Ù„Ù„ØªØ¬Ø§Ø±Ø©'),
          ('CHK-RCV-003', 'Ø¨Ù†Ùƒ Ø§Ù„Ø¥Ù…Ø§Ø±Ø§Øª Ø¯Ø¨ÙŠ Ø§Ù„ÙˆØ·Ù†ÙŠ', 'Ù…ÙˆØ±Ø¯ Ø§Ù„Ø¥ØµÙ„Ø§Ø­ Ø§Ù„Ø³Ø±ÙŠØ¹'),
          ('CHK-ISS-004', 'Ø¨Ù†Ùƒ Ø§Ù„Ø¥Ù…Ø§Ø±Ø§Øª Ø¯Ø¨ÙŠ Ø§Ù„ÙˆØ·Ù†ÙŠ', 'Ù…ÙˆØ±Ø¯ Ù…Ù„ØºÙ‰'),
          ('CHK-RCV-004', 'Ù…ØµØ±Ù Ø£Ø¨ÙˆØ¸Ø¨ÙŠ Ø§Ù„Ø¥Ø³Ù„Ø§Ù…ÙŠ', 'Ù…Ø¤Ø³Ø³Ø© Ù†Ø¬Ù… Ø§Ù„ØµØ­Ø±Ø§Ø¡')
      ) AS v(check_number, bank_name, party_name)
WHERE checks.check_number = v.check_number;

-- ---------------------------------------------------------------------------
-- Bank accounts: Arabic display names (keep account_number / IBAN as-is)
-- ---------------------------------------------------------------------------
UPDATE bank_accounts
SET bank_name = v.ar_name,
    updated_by = 'flyway',
    updated_at = NOW()
FROM (VALUES
          ('ERP Demo Bank', 'Ø¨Ù†Ùƒ ØªØ¬Ø±ÙŠØ¨ÙŠ Ù„Ù„Ù†Ø¸Ø§Ù…'),
          ('Main Operating Bank', 'Ø§Ù„Ø¨Ù†Ùƒ Ø§Ù„ØªØ´ØºÙŠÙ„ÙŠ Ø§Ù„Ø±Ø¦ÙŠØ³ÙŠ'),
          ('Emirates NBD', 'Ø¨Ù†Ùƒ Ø§Ù„Ø¥Ù…Ø§Ø±Ø§Øª Ø¯Ø¨ÙŠ Ø§Ù„ÙˆØ·Ù†ÙŠ'),
          ('Abu Dhabi Islamic Bank', 'Ù…ØµØ±Ù Ø£Ø¨ÙˆØ¸Ø¨ÙŠ Ø§Ù„Ø¥Ø³Ù„Ø§Ù…ÙŠ'),
          ('Bank Muscat', 'Ø¨Ù†Ùƒ Ù…Ø³Ù‚Ø·')
      ) AS v(en_name, ar_name)
WHERE TRIM(bank_accounts.bank_name) = v.en_name;

-- ---------------------------------------------------------------------------
-- Payment / receipt vouchers (V17 demo; no-op if V32 truncate removed rows)
-- ---------------------------------------------------------------------------
UPDATE payment_vouchers
SET description = v.description,
    party_name = v.party_name,
    updated_by = 'flyway',
    updated_at = NOW()
FROM (VALUES
          ('PV-DEMO-0001', 'Ø¯ÙØ¹ Ø±ÙˆØ§ØªØ¨ Ù…Ø§Ø±Ø³', 'Ø±ÙˆØ§ØªØ¨ Ø§Ù„ÙØ±ÙŠÙ‚'),
          ('PV-DEMO-0002', 'Ù…Ø³ØªØ­Ù‚Ø§Øª Ù‚Ø±Ø·Ø§Ø³ÙŠØ© Ù…Ø¹Ù„Ù‚Ø©', 'Ø£ÙˆÙÙŠØ³ Ù…Ø§Ø±Øª Ø°.Ù….Ù….'),
          ('PV-DEMO-0003', 'ØªØ³ÙˆÙŠØ© Ù…ØµØ±ÙˆÙ Ù…Ø³ØªØ­Ù‚ Ù…Ø³ÙˆØ¯Ø©', 'Ù…ÙˆØ±Ø¯ Ø§Ù„Ù…Ø±Ø§ÙÙ‚')
      ) AS v(reference, description, party_name)
WHERE payment_vouchers.reference = v.reference;

UPDATE receipt_vouchers
SET description = v.description,
    party_name = v.party_name,
    updated_by = 'flyway',
    updated_at = NOW()
FROM (VALUES
          ('RV-DEMO-0001', 'Ø¥ÙŠØµØ§Ù„ Ø¥ÙŠØ±Ø§Ø¯ Ø®Ø¯Ù…Ø§Øª', 'Ø¹Ù…ÙŠÙ„ Ø®Ø¯Ù…Ø§Øª Ø£ÙƒÙ…ÙŠ'),
          ('RV-DEMO-0002', 'ØªØ­ØµÙŠÙ„ Ø³Ù„ÙØ© Ø¹Ù…ÙŠÙ„', 'Ø§Ù„Ù…Ø­ÙŠØ· Ø§Ù„Ø£Ø²Ø±Ù‚ Ù„Ù„ØªØ¬Ø§Ø±Ø©'),
          ('RV-DEMO-0003', 'ØªØ­ØµÙŠÙ„ Ù†Ù‚Ø¯ÙŠ Ù…Ø³ÙˆØ¯Ø©', 'Ø¹Ù…ÙŠÙ„ Ù†Ù‚Ø¯ÙŠ')
      ) AS v(reference, description, party_name)
WHERE receipt_vouchers.reference = v.reference;

-- V17 journal line descriptions (if those rows still exist)
UPDATE journal_entry_lines
SET description = CASE BTRIM(description)
                      WHEN 'Accounts receivable debit' THEN 'Ù…Ø¯ÙŠÙ† Ø§Ù„Ø°Ù…Ù… Ø§Ù„Ù…Ø¯ÙŠÙ†Ø©'
                      WHEN 'Sales revenue credit' THEN 'Ø¯Ø§Ø¦Ù† Ø¥ÙŠØ±Ø§Ø¯ Ø§Ù„Ù…Ø¨ÙŠØ¹Ø§Øª'
                      WHEN 'Salary expense debit' THEN 'Ù…Ø¯ÙŠÙ† Ù…ØµØ±ÙˆÙ Ø§Ù„Ø±ÙˆØ§ØªØ¨'
                      WHEN 'Bank credit' THEN 'Ø¯Ø§Ø¦Ù† Ø§Ù„Ø¨Ù†Ùƒ'
                      WHEN 'Rent expense debit' THEN 'Ù…Ø¯ÙŠÙ† Ù…ØµØ±ÙˆÙ Ø§Ù„Ø¥ÙŠØ¬Ø§Ø±'
                      WHEN 'Utilities expense debit' THEN 'Ù…Ø¯ÙŠÙ† Ù…ØµØ±ÙˆÙ Ø§Ù„Ù…Ø±Ø§ÙÙ‚'
                      WHEN 'Bank debit' THEN 'Ù…Ø¯ÙŠÙ† Ø§Ù„Ø¨Ù†Ùƒ'
                      WHEN 'Service revenue credit' THEN 'Ø¯Ø§Ø¦Ù† Ø¥ÙŠØ±Ø§Ø¯ Ø§Ù„Ø®Ø¯Ù…Ø§Øª'
                      ELSE description
    END,
    updated_by = 'flyway',
    updated_at = NOW()
WHERE BTRIM(description) IN (
                             'Accounts receivable debit',
                             'Sales revenue credit',
                             'Salary expense debit',
                             'Bank credit',
                             'Rent expense debit',
                             'Utilities expense debit',
                             'Bank debit',
                             'Service revenue credit'
    );


-- ---------- source: V44__user_profiles_bilingual_names.sql ----------
SET search_path TO erp_system, public;

-- Bilingual display names for user profiles (Arabic + English columns; legacy full_name / company_name stay synced for sorting & old readers)

ALTER TABLE user_profiles
    ADD COLUMN IF NOT EXISTS full_name_en VARCHAR(150),
    ADD COLUMN IF NOT EXISTS full_name_ar VARCHAR(150),
    ADD COLUMN IF NOT EXISTS company_name_en VARCHAR(180),
    ADD COLUMN IF NOT EXISTS company_name_ar VARCHAR(180);

UPDATE user_profiles
SET full_name_en = full_name
WHERE full_name_en IS NULL;

UPDATE user_profiles
SET full_name_ar = full_name
WHERE full_name_ar IS NULL;

UPDATE user_profiles
SET company_name_en = company_name,
    company_name_ar = company_name
WHERE company_name IS NOT NULL
  AND TRIM(company_name) <> ''
  AND company_name_en IS NULL
  AND company_name_ar IS NULL;

ALTER TABLE user_profiles
    ALTER COLUMN full_name_en SET NOT NULL,
    ALTER COLUMN full_name_ar SET NOT NULL;

-- Legacy single column: default to English for reports / exports
UPDATE user_profiles
SET full_name = COALESCE(NULLIF(TRIM(full_name_en), ''), NULLIF(TRIM(full_name_ar), ''), full_name);

UPDATE user_profiles p
SET full_name_en    = v.name_en,
    full_name_ar    = v.name_ar,
    company_name_en = 'ERP Demo Company',
    company_name_ar = 'Ø´Ø±ÙƒØ© ØªØ¬Ø±ÙŠØ¨ÙŠØ© Ù„Ù„Ù†Ø¸Ø§Ù…',
    company_name    = 'ERP Demo Company',
    full_name       = v.name_en,
    updated_by      = 'flyway',
    updated_at      = NOW()
FROM users u,
     (VALUES
          ('chief.accountant', 'Chief Accountant', 'Ø§Ù„Ù…Ø­Ø§Ø³Ø¨ Ø§Ù„Ø±Ø¦ÙŠØ³ÙŠ'),
          ('treasury.user', 'Treasury Officer', 'Ù…Ø³Ø¤ÙˆÙ„ Ø§Ù„Ø®Ø²ÙŠÙ†Ø©'),
          ('report.viewer', 'Report Viewer', 'Ù…Ø³ØªØ¹Ø±Ø¶ Ø§Ù„ØªÙ‚Ø§Ø±ÙŠØ±'),
          ('finance.manager', 'Finance Manager', 'Ù…Ø¯ÙŠØ± Ø§Ù„Ø´Ø¤ÙˆÙ† Ø§Ù„Ù…Ø§Ù„ÙŠØ©')
      ) AS v(username, name_en, name_ar)
WHERE p.user_id = u.id
  AND u.username = v.username;

UPDATE user_profiles p
SET full_name_en    = 'ERP Administrator',
    full_name_ar    = 'Ù…Ø¯ÙŠØ± Ø§Ù„Ù†Ø¸Ø§Ù…',
    full_name       = 'ERP Administrator',
    updated_by      = 'flyway',
    updated_at      = NOW()
FROM users u
WHERE p.user_id = u.id
  AND u.username = 'admin';

-- Keep legacy single-language columns aligned with bilingual fields
UPDATE user_profiles
SET full_name    = COALESCE(NULLIF(TRIM(full_name_en), ''), NULLIF(TRIM(full_name_ar), ''), full_name),
    company_name = NULLIF(COALESCE(NULLIF(TRIM(company_name_en), ''), NULLIF(TRIM(company_name_ar), '')), '');


-- ---------- source: V45__system_management_menu_top_level.sql ----------
SET search_path TO erp_system, public;

-- System management (users, roles, lookups, screens) as its own top-level sidebar block,
-- not nested under NAV.HESABATY (accounting suite).
UPDATE ui_menu_items
SET parent_id = NULL,
    sort_order  = 1
WHERE id = 'admin-group';


-- ---------- source: V46__system_management_under_hesabaty.sql ----------
SET search_path TO erp_system, public;

-- Nest "System management" (users, roles, lookups, screens) under NAV.HESABATY again,
-- as its own collapsible block directly under Chart of accounts (sort_order 2).
-- Reverts the top-level placement from V45.

UPDATE ui_menu_items
SET sort_order = sort_order + 1
WHERE parent_id = 'hesabaty'
  AND sort_order >= 2;

UPDATE ui_menu_items
SET parent_id = 'hesabaty',
    sort_order  = 2
WHERE id = 'admin-group';


-- ---------- source: V47__system_management_root_group_module.sql ----------
SET search_path TO erp_system, public;

-- "System management" as its own top-level sidebar module (same pattern as NAV.HESABATY):
-- parent_id NULL, item_type = 'group', collapsible block with children (users, roles, â€¦).
-- Not nested under hesabaty and not a lone "collapse" row at the bottom of accounting items.

UPDATE ui_menu_items
SET parent_id = NULL,
    sort_order  = 1,
    item_type   = 'group'
WHERE id = 'admin-group';

