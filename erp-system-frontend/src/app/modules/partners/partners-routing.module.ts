import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { PartnersPageComponent } from './partners-page.component';
import { PartnerTransactionsPageComponent } from './partner-transactions-page.component';
import { DistributionsPageComponent } from './distributions-page.component';

const routes: Routes = [
  { path: '', component: PartnersPageComponent },
  { path: 'transactions', component: PartnerTransactionsPageComponent },
  { path: 'distributions', component: DistributionsPageComponent }
];

@NgModule({ imports: [RouterModule.forChild(routes)], exports: [RouterModule] })
export class PartnersRoutingModule {}
