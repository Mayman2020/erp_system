import { NgModule } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { TransactionsRoutingModule } from './transactions-routing.module';
import { TransactionsPageComponent } from './transactions-page.component';
@NgModule({ declarations: [TransactionsPageComponent], imports: [SharedModule, TransactionsRoutingModule] })
export class TransactionsModule {}

