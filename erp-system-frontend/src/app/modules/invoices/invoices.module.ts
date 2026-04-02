import { NgModule } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { InvoicesRoutingModule } from './invoices-routing.module';
import { InvoicesPageComponent } from './invoices-page.component';
@NgModule({ declarations: [InvoicesPageComponent], imports: [SharedModule, InvoicesRoutingModule] })
export class InvoicesModule {}

