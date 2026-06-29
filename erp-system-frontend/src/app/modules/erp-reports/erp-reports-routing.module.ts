import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { SalesReportPageComponent } from './sales-report-page.component';
import { PurchasesReportPageComponent } from './purchases-report-page.component';
import { InventoryReportPageComponent } from './inventory-report-page.component';
import { ProfitReportPageComponent } from './profit-report-page.component';

const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'sales' },
  { path: 'sales', component: SalesReportPageComponent },
  { path: 'purchases', component: PurchasesReportPageComponent },
  { path: 'inventory', component: InventoryReportPageComponent },
  { path: 'profit', component: ProfitReportPageComponent }
];

@NgModule({ imports: [RouterModule.forChild(routes)], exports: [RouterModule] })
export class ErpReportsRoutingModule {}
