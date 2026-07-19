import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { SharedModule } from '../../shared/shared.module';
import { NotificationsPageComponent } from './notifications-page.component';

const routes: Routes = [
  { path: '', component: NotificationsPageComponent }
];

@NgModule({
  declarations: [NotificationsPageComponent],
  imports: [SharedModule, RouterModule.forChild(routes)]
})
export class NotificationsModule {}
