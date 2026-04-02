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
