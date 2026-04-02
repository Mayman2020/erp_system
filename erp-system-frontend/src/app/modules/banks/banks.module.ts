import { NgModule } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { BanksRoutingModule } from './banks-routing.module';
import { BanksPageComponent } from './banks-page.component';

@NgModule({
  declarations: [BanksPageComponent],
  imports: [SharedModule, BanksRoutingModule]
})
export class BanksModule {}

