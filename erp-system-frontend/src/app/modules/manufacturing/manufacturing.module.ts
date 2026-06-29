import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { SharedModule } from '../../shared/shared.module';
import { ManufacturingPageComponent } from './manufacturing-page.component';
import { BomPageComponent } from './bom-page.component';

const routes: Routes = [
  { path: '', component: ManufacturingPageComponent },
  { path: 'bom', component: BomPageComponent }
];

@NgModule({
  declarations: [ManufacturingPageComponent, BomPageComponent],
  imports: [SharedModule, RouterModule.forChild(routes)]
})
export class ManufacturingModule {}
