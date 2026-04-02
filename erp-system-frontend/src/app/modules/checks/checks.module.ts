import { NgModule } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { ChecksRoutingModule } from './checks-routing.module';
import { ChecksPageComponent } from './checks-page.component';
@NgModule({ declarations: [ChecksPageComponent], imports: [SharedModule, ChecksRoutingModule] })
export class ChecksModule {}

