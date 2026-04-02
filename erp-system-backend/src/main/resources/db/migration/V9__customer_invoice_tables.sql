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
