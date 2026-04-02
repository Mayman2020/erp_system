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
