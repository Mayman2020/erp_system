import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ChecksPageComponent } from './checks-page.component';
const routes: Routes = [{ path: '', component: ChecksPageComponent }];
@NgModule({ imports: [RouterModule.forChild(routes)], exports: [RouterModule] })
export class ChecksRoutingModule {}

