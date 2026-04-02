import { NgModule } from '@angular/core';
import { SharedModule } from '../shared/shared.module';
import { AccountantsHomeComponent } from './accountants-home.component';
import { AccountantsRoutingModule } from './accountants-routing.module';

@NgModule({
  declarations: [AccountantsHomeComponent],
  imports: [SharedModule, AccountantsRoutingModule]
})
export class AccountantsModule {}
