import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { SuppliersPageComponent } from './suppliers-page.component';
import { OrdersPageComponent } from './orders-page.component';
import { InvoicesPageComponent } from './invoices-page.component';
import { ReturnsPageComponent } from './returns-page.component';
import { PaymentsPageComponent } from './payments-page.component';

const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'suppliers' },
  { path: 'suppliers', component: SuppliersPageComponent },
  { path: 'orders', component: OrdersPageComponent },
  { path: 'invoices', component: InvoicesPageComponent },
  { path: 'returns', component: ReturnsPageComponent },
  { path: 'payments', component: PaymentsPageComponent }
];

@NgModule({ imports: [RouterModule.forChild(routes)], exports: [RouterModule] })
export class PurchasesRoutingModule {}
