import { NgModule } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { PosRoutingModule } from './pos-routing.module';
import { PosSalePageComponent } from './pos-sale-page.component';
import { PosShiftsPageComponent } from './pos-shifts-page.component';
import { PosStartPageComponent } from './pos-start-page.component';

@NgModule({
  declarations: [PosStartPageComponent, PosSalePageComponent, PosShiftsPageComponent],
  imports: [SharedModule, PosRoutingModule]
})
export class PosModule {}
