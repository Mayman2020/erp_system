import { NgModule } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { BudgetRoutingModule } from './budget-routing.module';
import { BudgetPageComponent } from './budget-page.component';

@NgModule({
  declarations: [BudgetPageComponent],
  imports: [SharedModule, BudgetRoutingModule]
})
export class BudgetModule {}
