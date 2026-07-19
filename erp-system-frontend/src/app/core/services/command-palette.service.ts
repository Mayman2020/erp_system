import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export interface CommandPaletteItem {
  id: string;
  path: string;
  icon: string;
  labelKey: string;
  groupKey: string;
}

/** Static navigation targets (lazy routes — no API). */
export const COMMAND_PALETTE_ITEMS: CommandPaletteItem[] = [
  { id: 'dashboard', path: '/dashboard', icon: 'dashboard', labelKey: 'NAV.DASHBOARD', groupKey: 'COMMAND_PALETTE.GROUP_NAV' },
  { id: 'accounts', path: '/accounts', icon: 'account_tree', labelKey: 'NAV.CHART_OF_ACCOUNTS', groupKey: 'COMMAND_PALETTE.GROUP_NAV' },
  { id: 'journal', path: '/journal-entries', icon: 'receipt_long', labelKey: 'NAV.JOURNAL_ENTRIES', groupKey: 'COMMAND_PALETTE.GROUP_NAV' },
  { id: 'gl', path: '/general-ledger', icon: 'balance', labelKey: 'NAV.GENERAL_LEDGER', groupKey: 'COMMAND_PALETTE.GROUP_NAV' },
  { id: 'vouchers-pay', path: '/vouchers/payment', icon: 'payments', labelKey: 'NAV.PAYMENT_VOUCHERS', groupKey: 'COMMAND_PALETTE.GROUP_NAV' },
  { id: 'vouchers-rec', path: '/vouchers/receipt', icon: 'request_quote', labelKey: 'NAV.RECEIPT_VOUCHERS', groupKey: 'COMMAND_PALETTE.GROUP_NAV' },
  { id: 'transactions', path: '/transactions', icon: 'swap_horiz', labelKey: 'NAV.TRANSACTIONS', groupKey: 'COMMAND_PALETTE.GROUP_NAV' },
  { id: 'invoices', path: '/invoices', icon: 'description', labelKey: 'NAV.INVOICES', groupKey: 'COMMAND_PALETTE.GROUP_NAV' },
  { id: 'checks', path: '/checks', icon: 'payments', labelKey: 'NAV.CHECKS', groupKey: 'COMMAND_PALETTE.GROUP_NAV' },
  { id: 'ledger', path: '/ledger', icon: 'menu_book', labelKey: 'NAV.LEDGER', groupKey: 'COMMAND_PALETTE.GROUP_NAV' },
  { id: 'reports', path: '/reports', icon: 'assessment', labelKey: 'NAV.REPORTS', groupKey: 'COMMAND_PALETTE.GROUP_NAV' },
  { id: 'banks', path: '/bank-accounts', icon: 'account_balance', labelKey: 'NAV.BANK_ACCOUNTS', groupKey: 'COMMAND_PALETTE.GROUP_NAV' },
  { id: 'recon', path: '/reconciliation', icon: 'sync_alt', labelKey: 'NAV.RECONCILIATION', groupKey: 'COMMAND_PALETTE.GROUP_NAV' },
  { id: 'bills', path: '/bills', icon: 'description', labelKey: 'BILLS.TITLE', groupKey: 'COMMAND_PALETTE.GROUP_NAV' },
  { id: 'budget', path: '/budget', icon: 'pie_chart', labelKey: 'BUDGET.TITLE', groupKey: 'COMMAND_PALETTE.GROUP_NAV' },
  { id: 'exchange-rates', path: '/exchange-rates', icon: 'currency_exchange', labelKey: 'EXCHANGE_RATES.TITLE', groupKey: 'COMMAND_PALETTE.GROUP_NAV' },
  { id: 'transfers', path: '/transfers', icon: 'swap_horizontal_circle', labelKey: 'TRANSFERS.TITLE', groupKey: 'COMMAND_PALETTE.GROUP_NAV' },
  { id: 'settings', path: '/settings', icon: 'settings', labelKey: 'NAV.SETTINGS', groupKey: 'COMMAND_PALETTE.GROUP_NAV' },

  { id: 'inventory', path: '/inventory/products', icon: 'inventory_2', labelKey: 'MENU.INVENTORY', groupKey: 'COMMAND_PALETTE.GROUP_MODULES' },
  { id: 'sales', path: '/sales/customers', icon: 'point_of_sale', labelKey: 'MENU.SALES', groupKey: 'COMMAND_PALETTE.GROUP_MODULES' },
  { id: 'purchases', path: '/purchases/suppliers', icon: 'shopping_cart', labelKey: 'MENU.PURCHASES', groupKey: 'COMMAND_PALETTE.GROUP_MODULES' },
  { id: 'hr', path: '/hr/employees', icon: 'badge', labelKey: 'MENU.HR', groupKey: 'COMMAND_PALETTE.GROUP_MODULES' },
  { id: 'crm', path: '/crm/leads', icon: 'groups', labelKey: 'MENU.CRM', groupKey: 'COMMAND_PALETTE.GROUP_MODULES' },
  { id: 'manufacturing', path: '/manufacturing', icon: 'precision_manufacturing', labelKey: 'MENU.MANUFACTURING', groupKey: 'COMMAND_PALETTE.GROUP_MODULES' },
  { id: 'projects', path: '/projects', icon: 'assignment', labelKey: 'MENU.PROJECTS', groupKey: 'COMMAND_PALETTE.GROUP_MODULES' },

  { id: 'admin-users', path: '/admin/users', icon: 'people', labelKey: 'NAV.USERS', groupKey: 'COMMAND_PALETTE.GROUP_ADMIN' },
  { id: 'admin-roles', path: '/admin/roles', icon: 'shield', labelKey: 'NAV.ROLES', groupKey: 'COMMAND_PALETTE.GROUP_ADMIN' },
  { id: 'admin-lookups', path: '/admin/lookups', icon: 'list_alt', labelKey: 'NAV.LOOKUPS', groupKey: 'COMMAND_PALETTE.GROUP_ADMIN' },
  { id: 'admin-screens', path: '/admin/screens', icon: 'view_quilt', labelKey: 'NAV.SCREENS', groupKey: 'COMMAND_PALETTE.GROUP_ADMIN' },
  { id: 'admin-user-access', path: '/admin/user-access', icon: 'admin_panel_settings', labelKey: 'NAV.USER_ACCESS', groupKey: 'COMMAND_PALETTE.GROUP_ADMIN' }
];

@Injectable({ providedIn: 'root' })
export class CommandPaletteService {
  private readonly openSubject = new BehaviorSubject(false);
  readonly open$ = this.openSubject.asObservable();

  get isOpen(): boolean {
    return this.openSubject.value;
  }

  open(): void {
    this.openSubject.next(true);
  }

  close(): void {
    this.openSubject.next(false);
  }

  toggle(): void {
    this.openSubject.next(!this.openSubject.value);
  }
}
