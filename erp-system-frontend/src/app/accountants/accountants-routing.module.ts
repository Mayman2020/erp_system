import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AccountantsHomeComponent } from './accountants-home.component';
import { UserAccessComponent } from './user-access.component';

const routes: Routes = [
  { path: '', redirectTo: 'users', pathMatch: 'full' },
  { path: 'users', component: AccountantsHomeComponent, data: { tab: 'users' } },
  { path: 'roles', component: AccountantsHomeComponent, data: { tab: 'roles' } },
  { path: 'lookups', component: AccountantsHomeComponent, data: { tab: 'lookups' } },
  { path: 'screens', component: AccountantsHomeComponent, data: { tab: 'screens' } },
  { path: 'user-access', component: UserAccessComponent },
  { path: 'profile', redirectTo: '/settings', pathMatch: 'full' }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AccountantsRoutingModule {}
