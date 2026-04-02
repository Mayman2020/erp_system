import { NgModule } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { ReportsRoutingModule } from './reports-routing.module';
import { ReportsPageComponent } from './reports-page.component';

@NgModule({
  declarations: [ReportsPageComponent],
  imports: [SharedModule, ReportsRoutingModule]
})
export class ReportsModule {}

