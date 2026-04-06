SET search_path TO erp_system, public;

-- Bilingual display names for user profiles (Arabic + English columns; legacy full_name / company_name stay synced for sorting & old readers)

ALTER TABLE user_profiles
    ADD COLUMN IF NOT EXISTS full_name_en VARCHAR(150),
    ADD COLUMN IF NOT EXISTS full_name_ar VARCHAR(150),
    ADD COLUMN IF NOT EXISTS company_name_en VARCHAR(180),
    ADD COLUMN IF NOT EXISTS company_name_ar VARCHAR(180);

UPDATE user_profiles
SET full_name_en = full_name
WHERE full_name_en IS NULL;

UPDATE user_profiles
SET full_name_ar = full_name
WHERE full_name_ar IS NULL;

UPDATE user_profiles
SET company_name_en = company_name,
    company_name_ar = company_name
WHERE company_name IS NOT NULL
  AND TRIM(company_name) <> ''
  AND company_name_en IS NULL
  AND company_name_ar IS NULL;

ALTER TABLE user_profiles
    ALTER COLUMN full_name_en SET NOT NULL,
    ALTER COLUMN full_name_ar SET NOT NULL;

-- Legacy single column: default to English for reports / exports
UPDATE user_profiles
SET full_name = COALESCE(NULLIF(TRIM(full_name_en), ''), NULLIF(TRIM(full_name_ar), ''), full_name);

UPDATE user_profiles p
SET full_name_en    = v.name_en,
    full_name_ar    = v.name_ar,
    company_name_en = 'ERP Demo Company',
    company_name_ar = 'شركة تجريبية للنظام',
    company_name    = 'ERP Demo Company',
    full_name       = v.name_en,
    updated_by      = 'flyway',
    updated_at      = NOW()
FROM users u,
     (VALUES
          ('chief.accountant', 'Chief Accountant', 'المحاسب الرئيسي'),
          ('treasury.user', 'Treasury Officer', 'مسؤول الخزينة'),
          ('report.viewer', 'Report Viewer', 'مستعرض التقارير'),
          ('finance.manager', 'Finance Manager', 'مدير الشؤون المالية')
      ) AS v(username, name_en, name_ar)
WHERE p.user_id = u.id
  AND u.username = v.username;

UPDATE user_profiles p
SET full_name_en    = 'ERP Administrator',
    full_name_ar    = 'مدير النظام',
    full_name       = 'ERP Administrator',
    updated_by      = 'flyway',
    updated_at      = NOW()
FROM users u
WHERE p.user_id = u.id
  AND u.username = 'admin';

-- Keep legacy single-language columns aligned with bilingual fields
UPDATE user_profiles
SET full_name    = COALESCE(NULLIF(TRIM(full_name_en), ''), NULLIF(TRIM(full_name_ar), ''), full_name),
    company_name = NULLIF(COALESCE(NULLIF(TRIM(company_name_en), ''), NULLIF(TRIM(company_name_ar), '')), '');
