import { NgModule } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { GeneralLedgerRoutingModule } from './general-ledger-routing.module';
import { GeneralLedgerListComponent } from './general-ledger-list.component';
import { GeneralLedgerDetailComponent } from './general-ledger-detail.component';

@NgModule({
  declarations: [GeneralLedgerListComponent, GeneralLedgerDetailComponent],
  imports: [SharedModule, GeneralLedgerRoutingModule]
})
export class GeneralLedgerModule {}
