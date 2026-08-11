import { NgModule } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { MaintenanceRoutingModule } from './maintenance-routing.module';
import { AssetsPageComponent } from './assets-page.component';
import { TicketsPageComponent } from './tickets-page.component';

@NgModule({
  declarations: [AssetsPageComponent, TicketsPageComponent],
  imports: [SharedModule, MaintenanceRoutingModule]
})
export class MaintenanceModule {}
