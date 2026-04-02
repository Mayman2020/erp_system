import { NgModule } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { TransfersRoutingModule } from './transfers-routing.module';
import { TransfersPageComponent } from './transfers-page.component';
@NgModule({ declarations: [TransfersPageComponent], imports: [SharedModule, TransfersRoutingModule] })
export class TransfersModule {}

