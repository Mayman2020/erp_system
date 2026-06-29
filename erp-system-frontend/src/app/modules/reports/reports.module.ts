import { NgModule } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { ReportsRoutingModule } from './reports-routing.module';
import { ReportsPageComponent } from './reports-page.component';
import { ErpReportsModule } from '../erp-reports/erp-reports.module';

@NgModule({
  declarations: [ReportsPageComponent],
  imports: [SharedModule, ReportsRoutingModule, ErpReportsModule]
})
export class ReportsModule {}

