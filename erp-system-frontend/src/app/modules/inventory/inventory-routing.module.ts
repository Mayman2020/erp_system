import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ProductsPageComponent } from './products-page.component';
import { CategoriesPageComponent } from './categories-page.component';
import { WarehousesPageComponent } from './warehouses-page.component';
import { StockLevelsPageComponent } from './stock-levels-page.component';
import { LowStockPageComponent } from './low-stock-page.component';
import { MovementsPageComponent } from './movements-page.component';
import { UnitsPageComponent } from './units-page.component';

const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'products' },
  { path: 'products', component: ProductsPageComponent },
  { path: 'categories', component: CategoriesPageComponent },
  { path: 'warehouses', component: WarehousesPageComponent },
  { path: 'units', component: UnitsPageComponent },
  { path: 'stock-levels', component: StockLevelsPageComponent },
  { path: 'low-stock', component: LowStockPageComponent },
  { path: 'movements', component: MovementsPageComponent }
];

@NgModule({ imports: [RouterModule.forChild(routes)], exports: [RouterModule] })
export class InventoryRoutingModule {}
