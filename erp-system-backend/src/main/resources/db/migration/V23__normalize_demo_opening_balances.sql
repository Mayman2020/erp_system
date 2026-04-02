SET search_path TO erp_system, public;

-- Align demo opening balances with the intended enterprise sample chart of accounts.
UPDATE accounts
SET opening_balance = CASE code
        WHEN '1110' THEN 2500.00
        WHEN '1120' THEN 35000.00
        WHEN '1130' THEN 12000.00
        WHEN '1200' THEN 18000.00
        WHEN '1300' THEN 22000.00
        WHEN '1500' THEN 85000.00
        WHEN '2100' THEN 14000.00
        WHEN '2200' THEN 6000.00
        WHEN '3100' THEN 90000.00
        WHEN '3200' THEN 64500.00
        ELSE opening_balance
    END,
    opening_balance_side = CASE code
        WHEN '1110' THEN 'DEBIT'
        WHEN '1120' THEN 'DEBIT'
        WHEN '1130' THEN 'DEBIT'
        WHEN '1200' THEN 'DEBIT'
        WHEN '1300' THEN 'DEBIT'
        WHEN '1500' THEN 'DEBIT'
        WHEN '2100' THEN 'CREDIT'
        WHEN '2200' THEN 'CREDIT'
        WHEN '3100' THEN 'CREDIT'
        WHEN '3200' THEN 'CREDIT'
        ELSE opening_balance_side
    END,
    updated_by = 'flyway'
WHERE code IN ('1110', '1120', '1130', '1200', '1300', '1500', '2100', '2200', '3100', '3200');

-- Stored bank balances should match the ledger-based current balance used by the services.
UPDATE bank_accounts b
SET current_balance = (
        COALESCE(b.opening_balance, 0)
        + COALESCE((
            SELECT SUM(line.debit - line.credit)
            FROM journal_entry_lines line
            JOIN journal_entries entry ON entry.id = line.journal_entry_id
            WHERE line.account_id = b.linked_account_id
              AND entry.status IN ('POSTED', 'REVERSED')
        ), 0)
    ),
    updated_by = 'flyway';
