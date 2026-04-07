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
  { id: 'settings', path: '/settings', icon: 'settings', labelKey: 'NAV.SETTINGS', groupKey: 'COMMAND_PALETTE.GROUP_NAV' },
  { id: 'accountants', path: '/accountants', icon: 'admin_panel_settings', labelKey: 'NAV.ACCOUNTANTS_PORTAL', groupKey: 'COMMAND_PALETTE.GROUP_NAV' }
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
