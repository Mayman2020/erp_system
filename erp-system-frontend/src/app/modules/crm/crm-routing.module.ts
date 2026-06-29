import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LeadsPageComponent } from './leads-page.component';
import { ActivitiesPageComponent } from './activities-page.component';

import { NotesPageComponent } from './notes-page.component';

const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'leads' },
  { path: 'leads', component: LeadsPageComponent },
  { path: 'activities', component: ActivitiesPageComponent },
  { path: 'notes', component: NotesPageComponent }
];

@NgModule({ imports: [RouterModule.forChild(routes)], exports: [RouterModule] })
export class CrmRoutingModule {}
