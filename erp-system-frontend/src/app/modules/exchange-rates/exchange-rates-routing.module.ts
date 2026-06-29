import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ExchangeRatesPageComponent } from './exchange-rates-page.component';

const routes: Routes = [{ path: '', component: ExchangeRatesPageComponent }];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ExchangeRatesRoutingModule {}
