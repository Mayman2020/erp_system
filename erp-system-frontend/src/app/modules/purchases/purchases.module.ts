import { NgModule } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { PurchasesRoutingModule } from './purchases-routing.module';
import { SuppliersPageComponent } from './suppliers-page.component';
import { OrdersPageComponent } from './orders-page.component';
import { InvoicesPageComponent } from './invoices-page.component';
import { ReturnsPageComponent } from './returns-page.component';
import { PaymentsPageComponent } from './payments-page.component';

@NgModule({
  declarations: [SuppliersPageComponent, OrdersPageComponent, InvoicesPageComponent, ReturnsPageComponent, PaymentsPageComponent],
  imports: [SharedModule, PurchasesRoutingModule]
})
export class PurchasesModule {}
