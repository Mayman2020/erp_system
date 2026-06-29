import { NgModule } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { ProjectsRoutingModule } from './projects-routing.module';
import { ProjectsPageComponent } from './projects-page.component';
import { ProjectDetailPanelComponent } from './project-detail-panel.component';

@NgModule({
  declarations: [ProjectsPageComponent, ProjectDetailPanelComponent],
  imports: [SharedModule, ProjectsRoutingModule]
})
export class ProjectsModule {}
