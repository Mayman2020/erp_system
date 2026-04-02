-- Fix journal entry description stored as BYTEA (breaks LOWER() in JPQL search on PostgreSQL).
SET search_path TO erp_system, public;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'erp_system'
          AND table_name = 'journal_entries'
          AND column_name = 'description'
          AND udt_name = 'bytea'
    ) THEN
        ALTER TABLE journal_entries
            ALTER COLUMN description TYPE VARCHAR(1000)
                USING (
                    CASE
                        WHEN description IS NULL THEN NULL
                        WHEN octet_length(description) = 0 THEN NULL
                        ELSE substring(convert_from(description, 'UTF8') FROM 1 FOR 1000)
                    END
                );
    END IF;

    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'erp_system'
          AND table_name = 'journal_entry_lines'
          AND column_name = 'description'
          AND udt_name = 'bytea'
    ) THEN
        ALTER TABLE journal_entry_lines
            ALTER COLUMN description TYPE VARCHAR(1000)
                USING (
                    CASE
                        WHEN description IS NULL THEN NULL
                        WHEN octet_length(description) = 0 THEN NULL
                        ELSE substring(convert_from(description, 'UTF8') FROM 1 FOR 1000)
                    END
                );
    END IF;
END $$;
