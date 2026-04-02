SET search_path TO erp_system, public;

UPDATE accounts
SET
    name_en = COALESCE(NULLIF(BTRIM(name_en), ''), NULLIF(BTRIM(name), '')),
    name_ar = COALESCE(NULLIF(BTRIM(name_ar), ''), NULLIF(BTRIM(name_en), ''), NULLIF(BTRIM(name), '')),
    name = COALESCE(NULLIF(BTRIM(name), ''), NULLIF(BTRIM(name_en), ''), NULLIF(BTRIM(name_ar), ''), code)
WHERE
    name IS NULL OR BTRIM(name) = ''
    OR name_en IS NULL OR BTRIM(name_en) = ''
    OR name_ar IS NULL OR BTRIM(name_ar) = '';
