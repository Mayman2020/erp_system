import { NgModule } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { ErpReportsRoutingModule } from './erp-reports-routing.module';
import { SalesReportPageComponent } from './sales-report-page.component';
import { PurchasesReportPageComponent } from './purchases-report-page.component';
import { InventoryReportPageComponent } from './inventory-report-page.component';
import { ProfitReportPageComponent } from './profit-report-page.component';

@NgModule({
  declarations: [SalesReportPageComponent, PurchasesReportPageComponent, InventoryReportPageComponent, ProfitReportPageComponent],
  imports: [SharedModule, ErpReportsRoutingModule],
  exports: [SalesReportPageComponent, PurchasesReportPageComponent, InventoryReportPageComponent, ProfitReportPageComponent]
})
export class ErpReportsModule {}
