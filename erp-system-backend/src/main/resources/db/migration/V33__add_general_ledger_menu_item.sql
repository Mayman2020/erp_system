SET search_path TO erp_system, public;

INSERT INTO ui_menu_items (id, parent_id, sort_order, item_type, title_key, icon, url, is_external, target_blank, roles_csv, item_classes, breadcrumbs_flag)
VALUES ('general-ledger', 'hesabaty', 2, 'item', 'NAV.GENERAL_LEDGER', 'auto_stories', '/general-ledger', FALSE, FALSE, 'ADMIN,ACCOUNTANT', 'nav-item', FALSE)
ON CONFLICT (id) DO UPDATE
SET parent_id       = EXCLUDED.parent_id,
    sort_order      = EXCLUDED.sort_order,
    item_type       = EXCLUDED.item_type,
    title_key       = EXCLUDED.title_key,
    icon            = EXCLUDED.icon,
    url             = EXCLUDED.url,
    roles_csv       = EXCLUDED.roles_csv,
    item_classes    = EXCLUDED.item_classes;

UPDATE ui_menu_items SET sort_order = sort_order + 1
WHERE parent_id = 'hesabaty'
  AND id <> 'general-ledger'
  AND id <> 'dashboard'
  AND id <> 'accounts'
  AND sort_order >= 2;
