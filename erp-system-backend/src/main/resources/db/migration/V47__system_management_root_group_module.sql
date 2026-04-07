SET search_path TO erp_system, public;

-- "System management" as its own top-level sidebar module (same pattern as NAV.HESABATY):
-- parent_id NULL, item_type = 'group', collapsible block with children (users, roles, …).
-- Not nested under hesabaty and not a lone "collapse" row at the bottom of accounting items.

UPDATE ui_menu_items
SET parent_id = NULL,
    sort_order  = 1,
    item_type   = 'group'
WHERE id = 'admin-group';
