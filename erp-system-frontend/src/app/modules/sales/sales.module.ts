import { NgModule } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { SalesRoutingModule } from './sales-routing.module';
import { CustomersPageComponent } from './customers-page.component';
import { QuotationsPageComponent } from './quotations-page.component';
import { OrdersPageComponent } from './orders-page.component';
import { InvoicesPageComponent } from './invoices-page.component';
import { ReturnsPageComponent } from './returns-page.component';

@NgModule({
  declarations: [CustomersPageComponent, QuotationsPageComponent, OrdersPageComponent, InvoicesPageComponent, ReturnsPageComponent],
  imports: [SharedModule, SalesRoutingModule]
})
export class SalesModule {}
