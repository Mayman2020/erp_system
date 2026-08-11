import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { PermissionGuard } from '../../core/auth/permission.guard';
import { AssetsPageComponent } from './assets-page.component';
import { TicketsPageComponent } from './tickets-page.component';

const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'tickets' },
  { path: 'tickets', canActivate: [PermissionGuard], data: { menuItemId: 'erp-maintenance-tickets' }, component: TicketsPageComponent },
  { path: 'assets', canActivate: [PermissionGuard], data: { menuItemId: 'erp-maintenance-assets' }, component: AssetsPageComponent }
];

@NgModule({ imports: [RouterModule.forChild(routes)], exports: [RouterModule] })
export class MaintenanceRoutingModule {}
