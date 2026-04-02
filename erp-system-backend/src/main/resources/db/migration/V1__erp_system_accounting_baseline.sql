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
