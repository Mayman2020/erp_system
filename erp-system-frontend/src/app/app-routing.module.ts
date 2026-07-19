import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { AdminComponent } from './theme/layout/admin/admin.component';
import {AuthComponent} from './theme/layout/auth/auth.component';
import { AuthGuard } from './core/auth/auth.guard';
import { AdminGuard } from './core/auth/admin.guard';
import { PermissionGuard } from './core/auth/permission.guard';
import { MustChangePasswordGuard } from './core/auth/must-change-password.guard';
import { ForcePasswordChangeComponent } from './modules/auth/force-password-change/force-password-change.component';
import { NotFoundComponent } from './shared/components/not-found/not-found.component';

const routes: Routes = [
  {
    // AuthGuard bounces unauthenticated users to /auth/signin; authenticated users land on the dashboard.
    path: '',
    redirectTo: 'dashboard',
    pathMatch: 'full'
  },
  {
    path: 'auth',
    component: AuthComponent,
    children: [
      {
        path: '',
        loadChildren: () => import('./modules/auth/auth.module').then(module => module.AuthModule)
      }
    ]
  },
  {
    path: '',
    component: AdminComponent,
    canActivate: [AuthGuard],
    canActivateChild: [MustChangePasswordGuard],
    children: [
      {
        path: 'force-password-change',
        component: ForcePasswordChangeComponent
      },
      {
        path: 'dashboard',
        loadChildren: () => import('./modules/dashboard/dashboard.module').then(module => module.DashboardModule)
      },
      {
        path: 'accounts',
        canActivate: [PermissionGuard],
        data: { menuItemId: 'accounts' },
        loadChildren: () => import('./modules/accounts/accounts.module').then(module => module.AccountsModule)
      },
      {
        path: 'journal-entry',
        redirectTo: 'journal-entries',
        pathMatch: 'full'
      },
      {
        path: 'journal-vouchers',
        redirectTo: 'journal-entries',
        pathMatch: 'full'
      },
      {
        path: 'journal-entries',
        canActivate: [PermissionGuard],
        data: { menuItemId: 'journal-entries' },
        loadChildren: () => import('./modules/journal-entry/journal-entry.module').then(module => module.JournalEntryModule)
      },
      {
        path: 'general-ledger',
        canActivate: [PermissionGuard],
        data: { menuItemId: 'general-ledger' },
        loadChildren: () => import('./modules/general-ledger/general-ledger.module').then(module => module.GeneralLedgerModule)
      },
      {
        path: 'payment-vouchers',
        redirectTo: 'vouchers/payment',
        pathMatch: 'full'
      },
      {
        path: 'receipt-vouchers',
        redirectTo: 'vouchers/receipt',
        pathMatch: 'full'
      },
      {
        path: 'vouchers',
        loadChildren: () => import('./modules/vouchers/vouchers.module').then(module => module.VouchersModule)
      },
      {
        path: 'transactions',
        canActivate: [PermissionGuard],
        data: { menuItemId: 'transactions' },
        loadChildren: () => import('./modules/transactions/transactions.module').then(module => module.TransactionsModule)
      },
      {
        path: 'invoices',
        canActivate: [PermissionGuard],
        data: { menuItemId: 'invoices' },
        loadChildren: () => import('./modules/invoices/invoices.module').then(module => module.InvoicesModule)
      },
      {
        path: 'checks',
        canActivate: [PermissionGuard],
        data: { menuItemId: 'checks' },
        loadChildren: () => import('./modules/checks/checks.module').then(module => module.ChecksModule)
      },
      {
        path: 'ledger',
        canActivate: [PermissionGuard],
        data: { menuItemId: 'ledger' },
        loadChildren: () => import('./modules/ledger/ledger.module').then(module => module.LedgerModule)
      },
      {
        path: 'reports',
        canActivate: [PermissionGuard],
        data: { menuItemId: 'reports' },
        loadChildren: () => import('./modules/reports/reports.module').then(module => module.ReportsModule)
      },
      {
        path: 'inventory',
        canActivate: [PermissionGuard],
        data: { menuItemId: 'erp-inventory-products' },
        loadChildren: () => import('./modules/inventory/inventory.module').then(module => module.InventoryModule)
      },
      {
        path: 'sales',
        canActivate: [PermissionGuard],
        data: { menuItemId: 'erp-sales-customers' },
        loadChildren: () => import('./modules/sales/sales.module').then(module => module.SalesModule)
      },
      {
        path: 'purchases',
        canActivate: [PermissionGuard],
        data: { menuItemId: 'erp-purchases-suppliers' },
        loadChildren: () => import('./modules/purchases/purchases.module').then(module => module.PurchasesModule)
      },
      {
        path: 'hr',
        canActivate: [PermissionGuard],
        data: { menuItemId: 'erp-hr-employees' },
        loadChildren: () => import('./modules/hr/hr.module').then(module => module.HrModule)
      },
      {
        path: 'crm',
        canActivate: [PermissionGuard],
        data: { menuItemId: 'erp-crm-leads' },
        loadChildren: () => import('./modules/crm/crm.module').then(module => module.CrmModule)
      },
      {
        path: 'manufacturing',
        canActivate: [PermissionGuard],
        data: { menuItemId: 'erp-manufacturing-orders' },
        loadChildren: () => import('./modules/manufacturing/manufacturing.module').then(module => module.ManufacturingModule)
      },
      {
        path: 'projects',
        canActivate: [PermissionGuard],
        data: { menuItemId: 'erp-projects-list' },
        loadChildren: () => import('./modules/projects/projects.module').then(module => module.ProjectsModule)
      },
      {
        path: 'erp-reports',
        redirectTo: 'reports',
        pathMatch: 'full'
      },
      {
        path: 'banks',
        redirectTo: 'bank-accounts',
        pathMatch: 'full'
      },
      {
        path: 'bank-accounts',
        canActivate: [PermissionGuard],
        data: { menuItemId: 'bank-accounts' },
        loadChildren: () => import('./modules/banks/banks.module').then(module => module.BanksModule)
      },
      {
        path: 'bills',
        canActivate: [PermissionGuard],
        data: { menuItemId: 'bills' },
        loadChildren: () => import('./modules/bills/bills.module').then(module => module.BillsModule)
      },
      {
        path: 'accounting/bills',
        redirectTo: 'bills',
        pathMatch: 'full'
      },
      {
        path: 'budget',
        canActivate: [PermissionGuard],
        data: { menuItemId: 'budget' },
        loadChildren: () => import('./modules/budget/budget.module').then(module => module.BudgetModule)
      },
      {
        path: 'accounting/budget',
        redirectTo: 'budget',
        pathMatch: 'full'
      },
      {
        path: 'exchange-rates',
        canActivate: [PermissionGuard],
        data: { menuItemId: 'exchange-rates' },
        loadChildren: () => import('./modules/exchange-rates/exchange-rates.module').then(module => module.ExchangeRatesModule)
      },
      {
        path: 'accounting/exchange-rates',
        redirectTo: 'exchange-rates',
        pathMatch: 'full'
      },
      {
        path: 'reconciliation',
        canActivate: [PermissionGuard],
        data: { menuItemId: 'reconciliation' },
        loadChildren: () => import('./modules/reconciliation/reconciliation.module').then(module => module.ReconciliationModule)
      },
      {
        path: 'transfers',
        canActivate: [PermissionGuard],
        data: { menuItemId: 'transfers' },
        loadChildren: () => import('./modules/transfers/transfers.module').then(module => module.TransfersModule)
      },
      {
        path: 'registers',
        redirectTo: 'ledger',
        pathMatch: 'full'
      },
      {
        path: 'erp/activity-log',
        canActivate: [PermissionGuard],
        data: { menuItemId: 'erp-activity-log' },
        loadChildren: () => import('./modules/activity-log/activity-log.module').then(module => module.ActivityLogModule)
      },
      {
        path: 'notifications',
        loadChildren: () => import('./modules/notifications/notifications.module').then(module => module.NotificationsModule)
      },
      {
        // Not permission-gated: every authenticated user must reach their own profile/password
        // tab here. The accounting sub-tab already gates itself via PermissionService.can('settings', ...).
        path: 'settings',
        loadChildren: () => import('./modules/settings/settings.module').then(module => module.SettingsModule)
      },
      {
        path: 'admin',
        canActivate: [AdminGuard],
        loadChildren: () => import('./accountants/accountants.module').then(module => module.AccountantsModule)
      },
      { path: 'accountants', redirectTo: 'admin', pathMatch: 'full' },
      { path: 'accountants/users', redirectTo: 'admin/users', pathMatch: 'full' },
      { path: 'accountants/roles', redirectTo: 'admin/roles', pathMatch: 'full' },
      { path: 'accountants/lookups', redirectTo: 'admin/lookups', pathMatch: 'full' },
      { path: 'accountants/screens', redirectTo: 'admin/screens', pathMatch: 'full' },
      // Catches any unmatched path for an authenticated user (AuthGuard above still applies,
      // so unauthenticated users are bounced to sign-in as before) — shows a real 404 inside
      // the app shell instead of silently redirecting to sign-in.
      { path: '**', component: NotFoundComponent }
    ]
  },
  {
    // Only reached for paths that don't match 'auth/*' or the shell's '' prefix at all
    // (practically unreachable today since the shell's own '**' above catches everything else).
    path: '**',
    redirectTo: 'auth/signin'
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
