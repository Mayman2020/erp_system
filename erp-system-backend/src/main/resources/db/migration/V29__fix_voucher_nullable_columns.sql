-- Fix: journal_entry_id must be nullable on vouchers because drafts have no journal entry yet
ALTER TABLE erp_system.payment_vouchers ALTER COLUMN journal_entry_id DROP NOT NULL;
ALTER TABLE erp_system.receipt_vouchers ALTER COLUMN journal_entry_id DROP NOT NULL;

-- Fix: account_id column added in V4 is NOT NULL but not mapped by JPA entities.
-- Make it nullable so Hibernate inserts don't fail, or drop if unused.
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'erp_system' AND table_name = 'payment_vouchers' AND column_name = 'account_id'
    ) THEN
        ALTER TABLE erp_system.payment_vouchers ALTER COLUMN account_id DROP NOT NULL;
        ALTER TABLE erp_system.payment_vouchers ALTER COLUMN account_id SET DEFAULT NULL;
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'erp_system' AND table_name = 'receipt_vouchers' AND column_name = 'account_id'
    ) THEN
        ALTER TABLE erp_system.receipt_vouchers ALTER COLUMN account_id DROP NOT NULL;
        ALTER TABLE erp_system.receipt_vouchers ALTER COLUMN account_id SET DEFAULT NULL;
    END IF;
END $$;
