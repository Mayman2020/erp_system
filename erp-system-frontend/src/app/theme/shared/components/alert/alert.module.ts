import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AlertComponent } from './alert.component';
import { NgbTooltipModule } from '@ng-bootstrap/ng-bootstrap';

@NgModule({
  imports: [
    CommonModule,
    NgbTooltipModule
  ],
  declarations: [AlertComponent],
  exports: [AlertComponent]
})
export class AlertModule { }
