import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { VouchersPageComponent } from './vouchers-page.component';

const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'payment' },
  { path: ':kind', component: VouchersPageComponent }
];

@NgModule({ imports: [RouterModule.forChild(routes)], exports: [RouterModule] })
export class VouchersRoutingModule {}
