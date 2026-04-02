import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ReconciliationPageComponent } from './reconciliation-page.component';

const routes: Routes = [{ path: '', component: ReconciliationPageComponent }];

@NgModule({ imports: [RouterModule.forChild(routes)], exports: [RouterModule] })
export class ReconciliationRoutingModule {}

