import { NgModule } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { ExchangeRatesRoutingModule } from './exchange-rates-routing.module';
import { ExchangeRatesPageComponent } from './exchange-rates-page.component';

@NgModule({
  declarations: [ExchangeRatesPageComponent],
  imports: [SharedModule, ExchangeRatesRoutingModule]
})
export class ExchangeRatesModule {}
