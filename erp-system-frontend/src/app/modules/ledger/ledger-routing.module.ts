import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LedgerPageComponent } from './ledger-page.component';
const routes: Routes = [{ path: '', component: LedgerPageComponent }];
@NgModule({ imports: [RouterModule.forChild(routes)], exports: [RouterModule] })
export class LedgerRoutingModule {}

