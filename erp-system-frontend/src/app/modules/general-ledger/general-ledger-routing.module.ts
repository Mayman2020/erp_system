import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { GeneralLedgerListComponent } from './general-ledger-list.component';
import { GeneralLedgerDetailComponent } from './general-ledger-detail.component';

const routes: Routes = [
  { path: '', component: GeneralLedgerListComponent },
  { path: ':id', component: GeneralLedgerDetailComponent }
];

@NgModule({ imports: [RouterModule.forChild(routes)], exports: [RouterModule] })
export class GeneralLedgerRoutingModule {}
