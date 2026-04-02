SET search_path TO erp_system, public;

ALTER TABLE accounts
    ADD COLUMN IF NOT EXISTS name_ar VARCHAR(150),
    ADD COLUMN IF NOT EXISTS name_en VARCHAR(150);

UPDATE accounts
SET
    name_en = COALESCE(NULLIF(name_en, ''), name),
    name_ar = COALESCE(
        NULLIF(name_ar, ''),
        CASE code
            WHEN '1000' THEN 'الأصول'
            WHEN '1100' THEN 'النقدية وما في حكمها'
            WHEN '1110' THEN 'النقدية بالصندوق'
            WHEN '1120' THEN 'الحساب البنكي'
            WHEN '2000' THEN 'الالتزامات'
            WHEN '3000' THEN 'حقوق الملكية'
            WHEN '4000' THEN 'الإيرادات التشغيلية'
            WHEN '4100' THEN 'إيرادات الخدمات'
            WHEN '5000' THEN 'المصروفات التشغيلية'
            WHEN '5100' THEN 'مصروفات المكتب'
            ELSE name
        END
    );

ALTER TABLE accounts
    ALTER COLUMN name_ar SET NOT NULL,
    ALTER COLUMN name_en SET NOT NULL;
