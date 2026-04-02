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
