import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { AdminComponent } from './theme/layout/admin/admin.component';
import {AuthComponent} from './theme/layout/auth/auth.component';
import { AuthGuard } from './core/auth/auth.guard';
import { AdminGuard } from './core/auth/admin.guard';
import { PermissionGuard } from './core/auth/permission.guard';

const routes: Routes = [
  {
    path: '',
    redirectTo: 'auth/signin',
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
    children: [
      {
        path: 'dashboard',
        loadChildren: () => import('./modules/dashboard/dashboard.module').then(module => module.DashboardModule)
      },
      {
        path: 'accounts',
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
        loadChildren: () => import('./modules/journal-entry/journal-entry.module').then(module => module.JournalEntryModule)
      },
      {
        path: 'general-ledger',
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
        loadChildren: () => import('./modules/transactions/transactions.module').then(module => module.TransactionsModule)
      },
      {
        path: 'invoices',
        loadChildren: () => import('./modules/invoices/invoices.module').then(module => module.InvoicesModule)
      },
      {
        path: 'checks',
        loadChildren: () => import('./modules/checks/checks.module').then(module => module.ChecksModule)
      },
      {
        path: 'ledger',
        loadChildren: () => import('./modules/ledger/ledger.module').then(module => module.LedgerModule)
      },
      {
        path: 'reports',
        loadChildren: () => import('./modules/reports/reports.module').then(module => module.ReportsModule)
      },
      {
        path: 'inventory',
        loadChildren: () => import('./modules/inventory/inventory.module').then(module => module.InventoryModule)
      },
      {
        path: 'sales',
        loadChildren: () => import('./modules/sales/sales.module').then(module => module.SalesModule)
      },
      {
        path: 'purchases',
        loadChildren: () => import('./modules/purchases/purchases.module').then(module => module.PurchasesModule)
      },
      {
        path: 'hr',
        loadChildren: () => import('./modules/hr/hr.module').then(module => module.HrModule)
      },
      {
        path: 'crm',
        loadChildren: () => import('./modules/crm/crm.module').then(module => module.CrmModule)
      },
      {
        path: 'manufacturing',
        loadChildren: () => import('./modules/manufacturing/manufacturing.module').then(module => module.ManufacturingModule)
      },
      {
        path: 'projects',
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
        loadChildren: () => import('./modules/banks/banks.module').then(module => module.BanksModule)
      },
      {
        path: 'bills',
        loadChildren: () => import('./modules/bills/bills.module').then(module => module.BillsModule)
      },
      {
        path: 'accounting/bills',
        redirectTo: 'bills',
        pathMatch: 'full'
      },
      {
        path: 'budget',
        loadChildren: () => import('./modules/budget/budget.module').then(module => module.BudgetModule)
      },
      {
        path: 'accounting/budget',
        redirectTo: 'budget',
        pathMatch: 'full'
      },
      {
        path: 'exchange-rates',
        loadChildren: () => import('./modules/exchange-rates/exchange-rates.module').then(module => module.ExchangeRatesModule)
      },
      {
        path: 'accounting/exchange-rates',
        redirectTo: 'exchange-rates',
        pathMatch: 'full'
      },
      {
        path: 'reconciliation',
        loadChildren: () => import('./modules/reconciliation/reconciliation.module').then(module => module.ReconciliationModule)
      },
      {
        path: 'transfers',
        loadChildren: () => import('./modules/transfers/transfers.module').then(module => module.TransfersModule)
      },
      {
        path: 'registers',
        redirectTo: 'ledger',
        pathMatch: 'full'
      },
      {
        path: 'erp/activity-log',
        loadChildren: () => import('./modules/activity-log/activity-log.module').then(module => module.ActivityLogModule)
      },
      {
        path: 'settings',
        loadChildren: () => import('./modules/settings/settings.module').then(module => module.SettingsModule)
      },
      {
        path: 'accountants',
        canActivate: [AdminGuard],
        loadChildren: () => import('./accountants/accountants.module').then(module => module.AccountantsModule)
      }
    ]
  },
  {
    path: '**',
    redirectTo: 'auth/signin'
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
