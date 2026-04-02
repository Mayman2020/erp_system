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
