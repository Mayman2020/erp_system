import { NgModule } from '@angular/core';
import { SharedModule } from '../shared/shared.module';
import { AccountantsHomeComponent } from './accountants-home.component';
import { AccountantsRoutingModule } from './accountants-routing.module';
import { UserAccessComponent } from './user-access.component';
import { LicensePageComponent } from './license-page.component';
import { BackupsPageComponent } from './backups-page.component';

@NgModule({
  declarations: [AccountantsHomeComponent, UserAccessComponent, LicensePageComponent, BackupsPageComponent],
  imports: [SharedModule, AccountantsRoutingModule]
})
export class AccountantsModule {}
