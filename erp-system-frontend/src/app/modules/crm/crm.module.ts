import { NgModule } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { CrmRoutingModule } from './crm-routing.module';
import { LeadsPageComponent } from './leads-page.component';
import { ActivitiesPageComponent } from './activities-page.component';
import { NotesPageComponent } from './notes-page.component';

@NgModule({
  declarations: [LeadsPageComponent, ActivitiesPageComponent, NotesPageComponent],
  imports: [SharedModule, CrmRoutingModule]
})
export class CrmModule {}
