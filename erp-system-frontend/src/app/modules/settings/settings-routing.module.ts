import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { SettingsPageComponent } from './settings-page.component';

const routes: Routes = [
  { path: '', component: SettingsPageComponent },
  { path: 'company', component: SettingsPageComponent, data: { tab: 'company' } }
];

@NgModule({ imports: [RouterModule.forChild(routes)], exports: [RouterModule] })
export class SettingsRoutingModule {}

