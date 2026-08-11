import { NgModule } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { PartnersRoutingModule } from './partners-routing.module';
import { PartnersPageComponent } from './partners-page.component';
import { PartnerTransactionsPageComponent } from './partner-transactions-page.component';
import { DistributionsPageComponent } from './distributions-page.component';

@NgModule({
  declarations: [PartnersPageComponent, PartnerTransactionsPageComponent, DistributionsPageComponent],
  imports: [SharedModule, PartnersRoutingModule]
})
export class PartnersModule {}
