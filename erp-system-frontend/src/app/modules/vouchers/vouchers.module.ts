import { NgModule } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { VouchersRoutingModule } from './vouchers-routing.module';
import { VouchersPageComponent } from './vouchers-page.component';

@NgModule({
  declarations: [VouchersPageComponent],
  imports: [SharedModule, VouchersRoutingModule]
})
export class VouchersModule {}

