import { NgModule } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { InventoryRoutingModule } from './inventory-routing.module';
import { ProductsPageComponent } from './products-page.component';
import { CategoriesPageComponent } from './categories-page.component';
import { WarehousesPageComponent } from './warehouses-page.component';
import { StockLevelsPageComponent } from './stock-levels-page.component';
import { MovementsPageComponent } from './movements-page.component';
import { UnitsPageComponent } from './units-page.component';
import { LowStockPageComponent } from './low-stock-page.component';
import { IncidentsPageComponent } from './incidents-page.component';
import { ReplenishmentPageComponent } from './replenishment-page.component';
import { LabelsPageComponent } from './labels-page.component';

@NgModule({
  declarations: [
    ProductsPageComponent, CategoriesPageComponent, WarehousesPageComponent, StockLevelsPageComponent,
    MovementsPageComponent, UnitsPageComponent, LowStockPageComponent,
    IncidentsPageComponent, ReplenishmentPageComponent, LabelsPageComponent
  ],
  imports: [SharedModule, InventoryRoutingModule]
})
export class InventoryModule {}
