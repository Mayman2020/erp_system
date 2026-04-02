SET search_path TO erp_system, public;

ALTER TABLE reconciliation_lines
    ADD COLUMN IF NOT EXISTS created_by VARCHAR(100),
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(100);
