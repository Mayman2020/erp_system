import {Injectable} from '@angular/core';

export interface NavigationItem {
  id: string;
  title: string;
  type: 'item' | 'collapse' | 'group';
  collapsible?: boolean;
  translate?: string;
  icon?: string;
  hidden?: boolean;
  url?: string;
  classes?: string;
  exactMatch?: boolean;
  external?: boolean;
  target?: boolean;
  breadcrumbs?: boolean;
  function?: any;
  badge?: {
    title?: string;
    type?: string;
  };
  children?: Navigation[];
}

export interface Navigation extends NavigationItem {
  children?: NavigationItem[];
}

const NavigationItems = [
  {
    id: 'hesabaty',
    title: 'NAV.HESABATY',
    type: 'group',
    icon: 'apartment',
    children: [
      { id: 'dashboard', title: 'NAV.DASHBOARD', type: 'item', url: '/dashboard', classes: 'nav-item', icon: 'space_dashboard' },
      { id: 'accounts', title: 'NAV.CHART_OF_ACCOUNTS', type: 'item', url: '/accounts', classes: 'nav-item', icon: 'account_tree' },
      { id: 'journal-entry', title: 'NAV.JOURNAL_ENTRIES', type: 'item', url: '/journal-entry', classes: 'nav-item', icon: 'menu_book' },
      { id: 'payment-vouchers', title: 'NAV.PAYMENT_VOUCHERS', type: 'item', url: '/vouchers/payment', classes: 'nav-item', icon: 'payments' },
      { id: 'receipt-vouchers', title: 'NAV.RECEIPT_VOUCHERS', type: 'item', url: '/vouchers/receipt', classes: 'nav-item', icon: 'receipt_long' },
      { id: 'transfers', title: 'NAV.TRANSFERS', type: 'item', url: '/transfers', classes: 'nav-item', icon: 'swap_horiz' },
      { id: 'transactions', title: 'NAV.TRANSACTIONS', type: 'item', url: '/transactions', classes: 'nav-item', icon: 'sync_alt' },
      { id: 'invoices', title: 'NAV.INVOICES', type: 'item', url: '/invoices', classes: 'nav-item', icon: 'request_quote' },
      { id: 'checks', title: 'NAV.CHECKS', type: 'item', url: '/checks', classes: 'nav-item', icon: 'rule' },
      { id: 'banks', title: 'NAV.BANK_ACCOUNTS', type: 'item', url: '/banks', classes: 'nav-item', icon: 'account_balance' },
      { id: 'ledger', title: 'NAV.LEDGER', type: 'item', url: '/ledger', classes: 'nav-item', icon: 'library_books' },
      { id: 'reconciliation', title: 'NAV.RECONCILIATION', type: 'item', url: '/reconciliation', classes: 'nav-item', icon: 'fact_check' },
      { id: 'reports', title: 'NAV.REPORTS', type: 'item', url: '/reports', classes: 'nav-item', icon: 'insert_chart' },
      { id: 'settings', title: 'NAV.SETTINGS', type: 'item', url: '/settings', classes: 'nav-item', icon: 'tune' },
      { id: 'accountants', title: 'NAV.ACCOUNTANTS_PORTAL', type: 'item', url: '/accountants', classes: 'nav-item', icon: 'groups' }
    ]
  }
];

@Injectable()
export class NavigationItem {
  public get() {
    return NavigationItems;
  }
}
