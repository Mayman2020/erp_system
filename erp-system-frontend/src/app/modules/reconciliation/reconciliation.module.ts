import { NgModule } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { ReconciliationRoutingModule } from './reconciliation-routing.module';
import { ReconciliationPageComponent } from './reconciliation-page.component';

@NgModule({
  declarations: [ReconciliationPageComponent],
  imports: [SharedModule, ReconciliationRoutingModule]
})
export class ReconciliationModule {}

