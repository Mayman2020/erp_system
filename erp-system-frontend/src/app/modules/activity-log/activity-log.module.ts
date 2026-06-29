import { NgModule } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { ActivityLogRoutingModule } from './activity-log-routing.module';
import { ActivityLogPageComponent } from './activity-log-page.component';

@NgModule({
  declarations: [ActivityLogPageComponent],
  imports: [SharedModule, ActivityLogRoutingModule]
})
export class ActivityLogModule {}
