import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { BanksPageComponent } from './banks-page.component';

const routes: Routes = [{ path: '', component: BanksPageComponent }];

@NgModule({ imports: [RouterModule.forChild(routes)], exports: [RouterModule] })
export class BanksRoutingModule {}

