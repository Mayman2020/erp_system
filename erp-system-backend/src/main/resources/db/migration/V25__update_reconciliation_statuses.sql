SET search_path TO erp_system, public;

UPDATE lookup_values 
SET code = 'IN_PROGRESS' 
WHERE type_code = 'reconciliation-statuses' AND code = 'FINALIZED';

UPDATE lookup_values 
SET code = 'COMPLETED' 
WHERE type_code = 'reconciliation-statuses' AND code = 'CANCELLED';

UPDATE reconciliations
SET status = 'IN_PROGRESS'
WHERE status = 'FINALIZED';

UPDATE reconciliations
SET status = 'COMPLETED'
WHERE status = 'CANCELLED';
