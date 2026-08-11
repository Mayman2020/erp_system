import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { SharedModule } from '../../shared/shared.module';
import { AlertsPageComponent } from './alerts-page.component';

const routes: Routes = [{ path: '', component: AlertsPageComponent }];

@NgModule({
  declarations: [AlertsPageComponent],
  imports: [SharedModule, RouterModule.forChild(routes)]
})
export class AlertsModule {}
