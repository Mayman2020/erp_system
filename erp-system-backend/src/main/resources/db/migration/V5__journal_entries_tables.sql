SET search_path TO erp_system, public;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'erp_system'
          AND table_name = 'journal_entries'
          AND column_name = 'reference'
    ) AND NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'erp_system'
          AND table_name = 'journal_entries'
          AND column_name = 'reference_number'
    ) THEN
        ALTER TABLE journal_entries RENAME COLUMN reference TO reference_number;
    END IF;
END $$;

ALTER TABLE journal_entries
    ADD COLUMN IF NOT EXISTS reference_number VARCHAR(50),
    ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    ADD COLUMN IF NOT EXISTS total_debit NUMERIC(19, 4) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS total_credit NUMERIC(19, 4) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS posted_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS posted_by VARCHAR(100),
    ADD COLUMN IF NOT EXISTS reversed_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS reversed_by VARCHAR(100),
    ADD COLUMN IF NOT EXISTS reversal_reference VARCHAR(50),
    ADD COLUMN IF NOT EXISTS source_module VARCHAR(40),
    ADD COLUMN IF NOT EXISTS source_record_id BIGINT;

UPDATE journal_entries
SET reference_number = COALESCE(reference_number, 'JE-' || id)
WHERE reference_number IS NULL;

ALTER TABLE journal_entries
    ALTER COLUMN reference_number SET NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'uq_journal_entries_reference_number'
          AND conrelid = 'erp_system.journal_entries'::regclass
    ) THEN
        ALTER TABLE journal_entries
            ADD CONSTRAINT uq_journal_entries_reference_number UNIQUE (reference_number);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_journal_entries_status'
          AND conrelid = 'erp_system.journal_entries'::regclass
    ) THEN
        ALTER TABLE journal_entries
            ADD CONSTRAINT chk_journal_entries_status
                CHECK (status IN ('DRAFT', 'POSTED', 'REVERSED', 'CANCELLED'));
    END IF;
END $$;

ALTER TABLE journal_entry_lines
    ADD COLUMN IF NOT EXISTS line_number INTEGER;

WITH numbered_lines AS (
    SELECT id,
           ROW_NUMBER() OVER (PARTITION BY journal_entry_id ORDER BY id) AS generated_line_number
    FROM journal_entry_lines
)
UPDATE journal_entry_lines line
SET line_number = numbered_lines.generated_line_number
FROM numbered_lines
WHERE line.id = numbered_lines.id
  AND line.line_number IS NULL;

ALTER TABLE journal_entry_lines
    ALTER COLUMN line_number SET NOT NULL;

UPDATE journal_entries header
SET total_debit = balances.total_debit,
    total_credit = balances.total_credit
FROM (
    SELECT journal_entry_id,
           COALESCE(SUM(debit), 0) AS total_debit,
           COALESCE(SUM(credit), 0) AS total_credit
    FROM journal_entry_lines
    GROUP BY journal_entry_id
) balances
WHERE header.id = balances.journal_entry_id;

CREATE INDEX IF NOT EXISTS idx_journal_entries_reference_number ON journal_entries(reference_number);
CREATE INDEX IF NOT EXISTS idx_journal_entries_status ON journal_entries(status);
CREATE INDEX IF NOT EXISTS idx_journal_entries_source ON journal_entries(source_module, source_record_id);
CREATE INDEX IF NOT EXISTS idx_journal_entry_lines_line_number ON journal_entry_lines(journal_entry_id, line_number);
