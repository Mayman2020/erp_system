import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ActivityLogPageComponent } from './activity-log-page.component';

const routes: Routes = [{ path: '', component: ActivityLogPageComponent }];

@NgModule({ imports: [RouterModule.forChild(routes)], exports: [RouterModule] })
export class ActivityLogRoutingModule {}
