SET search_path TO erp_system, public;

CREATE OR REPLACE FUNCTION erp_system.sync_accounts_name_legacy()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    NEW.name_en := COALESCE(NULLIF(BTRIM(NEW.name_en), ''), NULLIF(BTRIM(NEW.name), ''), NULLIF(BTRIM(NEW.name_ar), ''), NEW.code);
    NEW.name_ar := COALESCE(NULLIF(BTRIM(NEW.name_ar), ''), NEW.name_en);
    NEW.name := COALESCE(NULLIF(BTRIM(NEW.name), ''), NEW.name_en, NEW.name_ar, NEW.code);

    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_accounts_sync_legacy_name ON accounts;

CREATE TRIGGER trg_accounts_sync_legacy_name
BEFORE INSERT OR UPDATE ON accounts
FOR EACH ROW
EXECUTE FUNCTION erp_system.sync_accounts_name_legacy();

UPDATE accounts
SET
    name_en = COALESCE(NULLIF(BTRIM(name_en), ''), NULLIF(BTRIM(name), ''), NULLIF(BTRIM(name_ar), ''), code),
    name_ar = COALESCE(NULLIF(BTRIM(name_ar), ''), NULLIF(BTRIM(name_en), ''), NULLIF(BTRIM(name), ''), code),
    name = COALESCE(NULLIF(BTRIM(name), ''), NULLIF(BTRIM(name_en), ''), NULLIF(BTRIM(name_ar), ''), code)
WHERE
    name IS NULL OR BTRIM(name) = ''
    OR name_en IS NULL OR BTRIM(name_en) = ''
    OR name_ar IS NULL OR BTRIM(name_ar) = '';
