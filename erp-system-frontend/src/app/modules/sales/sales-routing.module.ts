import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { PermissionGuard } from '../../core/auth/permission.guard';
import { CustomersPageComponent } from './customers-page.component';
import { QuotationsPageComponent } from './quotations-page.component';
import { OrdersPageComponent } from './orders-page.component';
import { InvoicesPageComponent } from './invoices-page.component';
import { ReturnsPageComponent } from './returns-page.component';

const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'customers' },
  { path: 'customers', canActivate: [PermissionGuard], data: { menuItemId: 'erp-sales-customers' }, component: CustomersPageComponent },
  { path: 'quotations', canActivate: [PermissionGuard], data: { menuItemId: 'erp-sales-quotations' }, component: QuotationsPageComponent },
  { path: 'orders', canActivate: [PermissionGuard], data: { menuItemId: 'erp-sales-orders' }, component: OrdersPageComponent },
  { path: 'invoices', canActivate: [PermissionGuard], data: { menuItemId: 'erp-sales-invoices' }, component: InvoicesPageComponent },
  { path: 'returns', canActivate: [PermissionGuard], data: { menuItemId: 'erp-sales-returns' }, component: ReturnsPageComponent }
];

@NgModule({ imports: [RouterModule.forChild(routes)], exports: [RouterModule] })
export class SalesRoutingModule {}
