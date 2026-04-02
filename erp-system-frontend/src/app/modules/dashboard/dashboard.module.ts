import { NgModule } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { SharedModule as ThemeSharedModule } from '../../theme/shared/shared.module';
import { DashboardRoutingModule } from './dashboard-routing.module';
import { DashboardPageComponent } from './dashboard-page.component';

@NgModule({
  declarations: [DashboardPageComponent],
  imports: [SharedModule, ThemeSharedModule, DashboardRoutingModule]
})
export class DashboardModule {}

