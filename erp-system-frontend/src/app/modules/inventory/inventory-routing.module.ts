import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { PermissionGuard } from '../../core/auth/permission.guard';
import { ProductsPageComponent } from './products-page.component';
import { CategoriesPageComponent } from './categories-page.component';
import { WarehousesPageComponent } from './warehouses-page.component';
import { StockLevelsPageComponent } from './stock-levels-page.component';
import { LowStockPageComponent } from './low-stock-page.component';
import { MovementsPageComponent } from './movements-page.component';
import { UnitsPageComponent } from './units-page.component';
import { IncidentsPageComponent } from './incidents-page.component';
import { ReplenishmentPageComponent } from './replenishment-page.component';
import { LabelsPageComponent } from './labels-page.component';

const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'products' },
  { path: 'products', canActivate: [PermissionGuard], data: { menuItemId: 'erp-inventory-products' }, component: ProductsPageComponent },
  { path: 'categories', canActivate: [PermissionGuard], data: { menuItemId: 'erp-inventory-categories' }, component: CategoriesPageComponent },
  { path: 'warehouses', canActivate: [PermissionGuard], data: { menuItemId: 'erp-inventory-warehouses' }, component: WarehousesPageComponent },
  { path: 'units', canActivate: [PermissionGuard], data: { menuItemId: 'erp-inventory-units' }, component: UnitsPageComponent },
  { path: 'stock-levels', canActivate: [PermissionGuard], data: { menuItemId: 'erp-inventory-stock' }, component: StockLevelsPageComponent },
  { path: 'low-stock', canActivate: [PermissionGuard], data: { menuItemId: 'erp-inventory-low-stock' }, component: LowStockPageComponent },
  { path: 'movements', canActivate: [PermissionGuard], data: { menuItemId: 'erp-inventory-movements' }, component: MovementsPageComponent },
  { path: 'incidents', canActivate: [PermissionGuard], data: { menuItemId: 'erp-inventory-incidents' }, component: IncidentsPageComponent },
  { path: 'replenishment', canActivate: [PermissionGuard], data: { menuItemId: 'erp-inventory-replenishment' }, component: ReplenishmentPageComponent },
  { path: 'labels', canActivate: [PermissionGuard], data: { menuItemId: 'erp-inventory-labels' }, component: LabelsPageComponent }
];

@NgModule({ imports: [RouterModule.forChild(routes)], exports: [RouterModule] })
export class InventoryRoutingModule {}
