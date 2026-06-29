import { NgModule } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { BillsRoutingModule } from './bills-routing.module';
import { BillsPageComponent } from './bills-page.component';

@NgModule({
  declarations: [BillsPageComponent],
  imports: [SharedModule, BillsRoutingModule]
})
export class BillsModule {}
