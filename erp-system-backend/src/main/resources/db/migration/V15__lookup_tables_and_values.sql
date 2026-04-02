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
('reconciliation-statuses', 'FINALIZED', 2, TRUE, 'flyway', 'flyway'),
('reconciliation-statuses', 'CANCELLED', 3, TRUE, 'flyway', 'flyway'),
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
