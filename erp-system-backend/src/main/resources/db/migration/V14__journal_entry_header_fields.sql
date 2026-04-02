SET search_path TO erp_system, public;

ALTER TABLE journal_entries ADD COLUMN IF NOT EXISTS external_reference VARCHAR(80);
ALTER TABLE journal_entries ADD COLUMN IF NOT EXISTS currency_code VARCHAR(3) NOT NULL DEFAULT 'USD';
ALTER TABLE journal_entries ADD COLUMN IF NOT EXISTS entry_type VARCHAR(30) NOT NULL DEFAULT 'MANUAL';
