import { NgModule } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { SettingsRoutingModule } from './settings-routing.module';
import { SettingsPageComponent } from './settings-page.component';
import { ProfileSettingsComponent } from './components/profile-settings.component';
import { AccountingSettingsComponent } from './components/accounting-settings.component';

@NgModule({
  declarations: [SettingsPageComponent, ProfileSettingsComponent, AccountingSettingsComponent],
  imports: [SharedModule, SettingsRoutingModule]
})
export class SettingsModule {}
