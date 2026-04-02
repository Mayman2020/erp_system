import { NgModule } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { LedgerRoutingModule } from './ledger-routing.module';
import { LedgerPageComponent } from './ledger-page.component';
@NgModule({ declarations: [LedgerPageComponent], imports: [SharedModule, LedgerRoutingModule] })
export class LedgerModule {}

