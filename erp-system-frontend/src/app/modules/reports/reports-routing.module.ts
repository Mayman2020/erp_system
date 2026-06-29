import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ReportsPageComponent } from './reports-page.component';
import { SalesReportPageComponent } from '../erp-reports/sales-report-page.component';
import { PurchasesReportPageComponent } from '../erp-reports/purchases-report-page.component';
import { InventoryReportPageComponent } from '../erp-reports/inventory-report-page.component';
import { ProfitReportPageComponent } from '../erp-reports/profit-report-page.component';

const routes: Routes = [
  { path: '', component: ReportsPageComponent },
  { path: 'sales', component: SalesReportPageComponent },
  { path: 'purchases', component: PurchasesReportPageComponent },
  { path: 'inventory', component: InventoryReportPageComponent },
  { path: 'profit', component: ProfitReportPageComponent }
];

@NgModule({ imports: [RouterModule.forChild(routes)], exports: [RouterModule] })
export class ReportsRoutingModule {}

