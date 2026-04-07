SET search_path TO erp_system, public;

-- Nest "System management" (users, roles, lookups, screens) under NAV.HESABATY again,
-- as its own collapsible block directly under Chart of accounts (sort_order 2).
-- Reverts the top-level placement from V45.

UPDATE ui_menu_items
SET sort_order = sort_order + 1
WHERE parent_id = 'hesabaty'
  AND sort_order >= 2;

UPDATE ui_menu_items
SET parent_id = 'hesabaty',
    sort_order  = 2
WHERE id = 'admin-group';
