import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { PermissionGuard } from '../../core/auth/permission.guard';
import { SuppliersPageComponent } from './suppliers-page.component';
import { OrdersPageComponent } from './orders-page.component';
import { InvoicesPageComponent } from './invoices-page.component';
import { ReturnsPageComponent } from './returns-page.component';
import { PaymentsPageComponent } from './payments-page.component';
import { RfqsPageComponent } from './rfqs-page.component';
import { ReceiptsPageComponent } from './receipts-page.component';

const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'suppliers' },
  { path: 'suppliers', canActivate: [PermissionGuard], data: { menuItemId: 'erp-purchases-suppliers' }, component: SuppliersPageComponent },
  { path: 'orders', canActivate: [PermissionGuard], data: { menuItemId: 'erp-purchases-orders' }, component: OrdersPageComponent },
  { path: 'invoices', canActivate: [PermissionGuard], data: { menuItemId: 'erp-purchases-invoices' }, component: InvoicesPageComponent },
  { path: 'returns', canActivate: [PermissionGuard], data: { menuItemId: 'erp-purchases-returns' }, component: ReturnsPageComponent },
  { path: 'payments', canActivate: [PermissionGuard], data: { menuItemId: 'erp-purchases-payments' }, component: PaymentsPageComponent },
  { path: 'rfqs', canActivate: [PermissionGuard], data: { menuItemId: 'erp-purchases-rfqs' }, component: RfqsPageComponent },
  { path: 'receipts', canActivate: [PermissionGuard], data: { menuItemId: 'erp-purchases-receipts' }, component: ReceiptsPageComponent }
];

@NgModule({ imports: [RouterModule.forChild(routes)], exports: [RouterModule] })
export class PurchasesRoutingModule {}
