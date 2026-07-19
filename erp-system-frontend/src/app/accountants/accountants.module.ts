import { NgModule } from '@angular/core';
import { SharedModule } from '../shared/shared.module';
import { AccountantsHomeComponent } from './accountants-home.component';
import { AccountantsRoutingModule } from './accountants-routing.module';
import { UserAccessComponent } from './user-access.component';

@NgModule({
  declarations: [AccountantsHomeComponent, UserAccessComponent],
  imports: [SharedModule, AccountantsRoutingModule]
})
export class AccountantsModule {}
