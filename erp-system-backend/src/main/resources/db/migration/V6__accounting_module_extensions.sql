SET search_path TO erp_system, public;

ALTER TABLE payment_vouchers
    ADD COLUMN IF NOT EXISTS party_name VARCHAR(150),
    ADD COLUMN IF NOT EXISTS linked_document_reference VARCHAR(80),
    ADD COLUMN IF NOT EXISTS approved_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS approved_by VARCHAR(100),
    ADD COLUMN IF NOT EXISTS reversal_journal_entry_id BIGINT;

ALTER TABLE receipt_vouchers
    ADD COLUMN IF NOT EXISTS party_name VARCHAR(150),
    ADD COLUMN IF NOT EXISTS invoice_reference VARCHAR(80),
    ADD COLUMN IF NOT EXISTS approved_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS approved_by VARCHAR(100),
    ADD COLUMN IF NOT EXISTS reversal_journal_entry_id BIGINT;

ALTER TABLE transfers
    ADD COLUMN IF NOT EXISTS journal_entry_id BIGINT,
    ADD COLUMN IF NOT EXISTS reversal_journal_entry_id BIGINT;

ALTER TABLE transactions
    ADD COLUMN IF NOT EXISTS debit_account_id BIGINT,
    ADD COLUMN IF NOT EXISTS credit_account_id BIGINT,
    ADD COLUMN IF NOT EXISTS journal_entry_id BIGINT,
    ADD COLUMN IF NOT EXISTS original_transaction_id BIGINT,
    ADD COLUMN IF NOT EXISTS related_document_reference VARCHAR(80),
    ADD COLUMN IF NOT EXISTS posted_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS posted_by VARCHAR(100);

ALTER TABLE bills
    ADD COLUMN IF NOT EXISTS supplier_name VARCHAR(150),
    ADD COLUMN IF NOT EXISTS supplier_reference VARCHAR(80),
    ADD COLUMN IF NOT EXISTS payable_account_id BIGINT,
    ADD COLUMN IF NOT EXISTS tax_account_id BIGINT,
    ADD COLUMN IF NOT EXISTS journal_entry_id BIGINT,
    ADD COLUMN IF NOT EXISTS cancellation_journal_entry_id BIGINT,
    ADD COLUMN IF NOT EXISTS approved_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS approved_by VARCHAR(100),
    ADD COLUMN IF NOT EXISTS cancelled_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS cancelled_by VARCHAR(100);

ALTER TABLE bills
    ALTER COLUMN supplier_id DROP NOT NULL;

ALTER TABLE bill_lines
    ADD COLUMN IF NOT EXISTS created_by VARCHAR(100),
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(100);

ALTER TABLE checks
    ADD COLUMN IF NOT EXISTS bank_account_id BIGINT,
    ADD COLUMN IF NOT EXISTS holding_account_id BIGINT,
    ADD COLUMN IF NOT EXISTS journal_entry_id BIGINT,
    ADD COLUMN IF NOT EXISTS reversal_journal_entry_id BIGINT,
    ADD COLUMN IF NOT EXISTS linked_document_reference VARCHAR(80),
    ADD COLUMN IF NOT EXISTS cleared_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS bounced_at TIMESTAMPTZ;

ALTER TABLE reconciliation_lines
    ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'UNMATCHED',
    ADD COLUMN IF NOT EXISTS source_reference VARCHAR(80),
    ADD COLUMN IF NOT EXISTS journal_entry_line_id BIGINT,
    ADD COLUMN IF NOT EXISTS matched_line_id BIGINT,
    ADD COLUMN IF NOT EXISTS matched_amount NUMERIC(19, 2);

ALTER TABLE budgets
    ADD COLUMN IF NOT EXISTS budget_name VARCHAR(150),
    ADD COLUMN IF NOT EXISTS notes VARCHAR(500);

ALTER TABLE fiscal_years
    ADD COLUMN IF NOT EXISTS closed_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS closed_by VARCHAR(100);

ALTER TABLE fiscal_periods
    ADD COLUMN IF NOT EXISTS closed_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS closed_by VARCHAR(100);

ALTER TABLE accounting_settings
    ADD COLUMN IF NOT EXISTS created_by VARCHAR(100);

ALTER TABLE numbering_sequences
    ADD COLUMN IF NOT EXISTS created_by VARCHAR(100);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_payment_vouchers_reversal_entry'
          AND conrelid = 'erp_system.payment_vouchers'::regclass
    ) THEN
        ALTER TABLE payment_vouchers
            ADD CONSTRAINT fk_payment_vouchers_reversal_entry
                FOREIGN KEY (reversal_journal_entry_id) REFERENCES journal_entries (id);
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_receipt_vouchers_reversal_entry'
          AND conrelid = 'erp_system.receipt_vouchers'::regclass
    ) THEN
        ALTER TABLE receipt_vouchers
            ADD CONSTRAINT fk_receipt_vouchers_reversal_entry
                FOREIGN KEY (reversal_journal_entry_id) REFERENCES journal_entries (id);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_transfers_journal_entry'
          AND conrelid = 'erp_system.transfers'::regclass
    ) THEN
        ALTER TABLE transfers
            ADD CONSTRAINT fk_transfers_journal_entry
                FOREIGN KEY (journal_entry_id) REFERENCES journal_entries (id);
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_transfers_reversal_entry'
          AND conrelid = 'erp_system.transfers'::regclass
    ) THEN
        ALTER TABLE transfers
            ADD CONSTRAINT fk_transfers_reversal_entry
                FOREIGN KEY (reversal_journal_entry_id) REFERENCES journal_entries (id);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_transactions_debit_account'
          AND conrelid = 'erp_system.transactions'::regclass
    ) THEN
        ALTER TABLE transactions
            ADD CONSTRAINT fk_transactions_debit_account
                FOREIGN KEY (debit_account_id) REFERENCES accounts (id);
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_transactions_credit_account'
          AND conrelid = 'erp_system.transactions'::regclass
    ) THEN
        ALTER TABLE transactions
            ADD CONSTRAINT fk_transactions_credit_account
                FOREIGN KEY (credit_account_id) REFERENCES accounts (id);
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_transactions_journal_entry'
          AND conrelid = 'erp_system.transactions'::regclass
    ) THEN
        ALTER TABLE transactions
            ADD CONSTRAINT fk_transactions_journal_entry
                FOREIGN KEY (journal_entry_id) REFERENCES journal_entries (id);
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_transactions_original_transaction'
          AND conrelid = 'erp_system.transactions'::regclass
    ) THEN
        ALTER TABLE transactions
            ADD CONSTRAINT fk_transactions_original_transaction
                FOREIGN KEY (original_transaction_id) REFERENCES transactions (id);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_bills_payable_account'
          AND conrelid = 'erp_system.bills'::regclass
    ) THEN
        ALTER TABLE bills
            ADD CONSTRAINT fk_bills_payable_account
                FOREIGN KEY (payable_account_id) REFERENCES accounts (id);
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_bills_tax_account'
          AND conrelid = 'erp_system.bills'::regclass
    ) THEN
        ALTER TABLE bills
            ADD CONSTRAINT fk_bills_tax_account
                FOREIGN KEY (tax_account_id) REFERENCES accounts (id);
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_bills_journal_entry'
          AND conrelid = 'erp_system.bills'::regclass
    ) THEN
        ALTER TABLE bills
            ADD CONSTRAINT fk_bills_journal_entry
                FOREIGN KEY (journal_entry_id) REFERENCES journal_entries (id);
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_bills_cancellation_entry'
          AND conrelid = 'erp_system.bills'::regclass
    ) THEN
        ALTER TABLE bills
            ADD CONSTRAINT fk_bills_cancellation_entry
                FOREIGN KEY (cancellation_journal_entry_id) REFERENCES journal_entries (id);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_checks_bank_account'
          AND conrelid = 'erp_system.checks'::regclass
    ) THEN
        ALTER TABLE checks
            ADD CONSTRAINT fk_checks_bank_account
                FOREIGN KEY (bank_account_id) REFERENCES bank_accounts (id);
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_checks_holding_account'
          AND conrelid = 'erp_system.checks'::regclass
    ) THEN
        ALTER TABLE checks
            ADD CONSTRAINT fk_checks_holding_account
                FOREIGN KEY (holding_account_id) REFERENCES accounts (id);
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_checks_journal_entry'
          AND conrelid = 'erp_system.checks'::regclass
    ) THEN
        ALTER TABLE checks
            ADD CONSTRAINT fk_checks_journal_entry
                FOREIGN KEY (journal_entry_id) REFERENCES journal_entries (id);
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_checks_reversal_entry'
          AND conrelid = 'erp_system.checks'::regclass
    ) THEN
        ALTER TABLE checks
            ADD CONSTRAINT fk_checks_reversal_entry
                FOREIGN KEY (reversal_journal_entry_id) REFERENCES journal_entries (id);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'chk_reconciliation_lines_status'
          AND conrelid = 'erp_system.reconciliation_lines'::regclass
    ) THEN
        ALTER TABLE reconciliation_lines
            ADD CONSTRAINT chk_reconciliation_lines_status
                CHECK (status IN ('UNMATCHED', 'PARTIALLY_MATCHED', 'MATCHED'));
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_payment_vouchers_bill_id ON payment_vouchers(bill_id);
CREATE INDEX IF NOT EXISTS idx_payment_vouchers_method ON payment_vouchers(payment_method);
CREATE INDEX IF NOT EXISTS idx_receipt_vouchers_method ON receipt_vouchers(payment_method);
CREATE INDEX IF NOT EXISTS idx_transfers_journal_entry ON transfers(journal_entry_id);
CREATE INDEX IF NOT EXISTS idx_transactions_status ON transactions(status);
CREATE INDEX IF NOT EXISTS idx_transactions_document_reference ON transactions(related_document_reference);
CREATE INDEX IF NOT EXISTS idx_bills_journal_entry ON bills(journal_entry_id);
CREATE INDEX IF NOT EXISTS idx_checks_bank_account ON checks(bank_account_id);
CREATE INDEX IF NOT EXISTS idx_reconciliation_lines_status ON reconciliation_lines(status);
CREATE INDEX IF NOT EXISTS idx_reconciliation_lines_journal_line ON reconciliation_lines(journal_entry_line_id);

INSERT INTO numbering_sequences (sequence_name, prefix, current_number, padding_length)
VALUES
    ('TRANSACTION_REFERENCE', 'TXN-', 1, 6),
    ('BUDGET_REFERENCE', 'BDG-', 1, 6),
    ('BANK_RECONCILIATION', 'REC-', 1, 6)
ON CONFLICT (sequence_name) DO NOTHING;
