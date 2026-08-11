import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { PermissionGuard } from '../../core/auth/permission.guard';
import { PosSalePageComponent } from './pos-sale-page.component';
import { PosShiftsPageComponent } from './pos-shifts-page.component';
import { PosStartPageComponent } from './pos-start-page.component';

const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'start' },
  { path: 'start', canActivate: [PermissionGuard], data: { menuItemId: 'erp-pos' }, component: PosStartPageComponent },
  { path: 'sale', canActivate: [PermissionGuard], data: { menuItemId: 'erp-pos-sale' }, component: PosSalePageComponent },
  { path: 'shifts', canActivate: [PermissionGuard], data: { menuItemId: 'erp-pos-shifts' }, component: PosShiftsPageComponent }
];

@NgModule({ imports: [RouterModule.forChild(routes)], exports: [RouterModule] })
export class PosRoutingModule {}
