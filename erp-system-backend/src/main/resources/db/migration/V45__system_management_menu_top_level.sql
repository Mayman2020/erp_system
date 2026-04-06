SET search_path TO erp_system, public;

-- System management (users, roles, lookups, screens) as its own top-level sidebar block,
-- not nested under NAV.HESABATY (accounting suite).
UPDATE ui_menu_items
SET parent_id = NULL,
    sort_order  = 1
WHERE id = 'admin-group';
