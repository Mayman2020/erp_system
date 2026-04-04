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
