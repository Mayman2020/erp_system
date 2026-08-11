import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { SharedModule } from '../../shared/shared.module';
import { PmoPageComponent } from './pmo-page.component';

const routes: Routes = [{ path: '', component: PmoPageComponent }];

@NgModule({
  declarations: [PmoPageComponent],
  imports: [SharedModule, RouterModule.forChild(routes)]
})
export class PmoModule {}
