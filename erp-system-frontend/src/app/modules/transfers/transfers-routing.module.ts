import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { TransfersPageComponent } from './transfers-page.component';
const routes: Routes = [{ path: '', component: TransfersPageComponent }];
@NgModule({ imports: [RouterModule.forChild(routes)], exports: [RouterModule] })
export class TransfersRoutingModule {}

