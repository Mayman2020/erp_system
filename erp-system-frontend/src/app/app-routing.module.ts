import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { AdminComponent } from './theme/layout/admin/admin.component';
import {AuthComponent} from './theme/layout/auth/auth.component';
import { AuthGuard } from './core/auth/auth.guard';

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
        path: 'journal-entries',
        loadChildren: () => import('./modules/journal-entry/journal-entry.module').then(module => module.JournalEntryModule)
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
        path: 'transfers',
        loadChildren: () => import('./modules/transfers/transfers.module').then(module => module.TransfersModule)
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
        path: 'banks',
        redirectTo: 'bank-accounts',
        pathMatch: 'full'
      },
      {
        path: 'bank-accounts',
        loadChildren: () => import('./modules/banks/banks.module').then(module => module.BanksModule)
      },
      {
        path: 'reconciliation',
        loadChildren: () => import('./modules/reconciliation/reconciliation.module').then(module => module.ReconciliationModule)
      },
      {
        path: 'settings',
        loadChildren: () => import('./modules/settings/settings.module').then(module => module.SettingsModule)
      },
      {
        path: 'accountants',
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
