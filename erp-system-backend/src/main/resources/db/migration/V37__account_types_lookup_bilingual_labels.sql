SET search_path TO erp_system, public;

-- Ensure account-type lookup values have correct English + Arabic labels (admin UI lists both in DB; app shows one column by locale).
UPDATE lookup_values
SET name_en = 'Asset',
    name_ar = 'الأصول',
    updated_by = 'flyway',
    updated_at = NOW()
WHERE type_code = 'account-types'
  AND code = 'ASSET';

UPDATE lookup_values
SET name_en = 'Liability',
    name_ar = 'الالتزامات',
    updated_by = 'flyway',
    updated_at = NOW()
WHERE type_code = 'account-types'
  AND code = 'LIABILITY';

UPDATE lookup_values
SET name_en = 'Equity',
    name_ar = 'حقوق الملكية',
    updated_by = 'flyway',
    updated_at = NOW()
WHERE type_code = 'account-types'
  AND code = 'EQUITY';

UPDATE lookup_values
SET name_en = 'Revenue',
    name_ar = 'الإيرادات',
    updated_by = 'flyway',
    updated_at = NOW()
WHERE type_code = 'account-types'
  AND code IN ('REVENUE', 'INCOME');

UPDATE lookup_values
SET name_en = 'Expense',
    name_ar = 'المصروفات',
    updated_by = 'flyway',
    updated_at = NOW()
WHERE type_code = 'account-types'
  AND code = 'EXPENSE';
