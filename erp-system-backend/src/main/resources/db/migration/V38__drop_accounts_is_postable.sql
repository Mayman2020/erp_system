SET search_path TO erp_system, public;

ALTER TABLE accounts DROP COLUMN IF EXISTS is_postable;
