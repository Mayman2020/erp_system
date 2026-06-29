import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CustomersPageComponent } from './customers-page.component';
import { QuotationsPageComponent } from './quotations-page.component';
import { OrdersPageComponent } from './orders-page.component';
import { InvoicesPageComponent } from './invoices-page.component';
import { ReturnsPageComponent } from './returns-page.component';

const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'customers' },
  { path: 'customers', component: CustomersPageComponent },
  { path: 'quotations', component: QuotationsPageComponent },
  { path: 'orders', component: OrdersPageComponent },
  { path: 'invoices', component: InvoicesPageComponent },
  { path: 'returns', component: ReturnsPageComponent }
];

@NgModule({ imports: [RouterModule.forChild(routes)], exports: [RouterModule] })
export class SalesRoutingModule {}
