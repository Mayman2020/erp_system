import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BreadcrumbComponent } from './breadcrumb.component';
import { RouterModule } from '@angular/router';
import { SharedModule as ErpSharedModule } from '../../../../shared/shared.module';

@NgModule({
  imports: [
    CommonModule,
    RouterModule,
    ErpSharedModule
  ],
  declarations: [BreadcrumbComponent],
  exports: [BreadcrumbComponent]
})
export class BreadcrumbModule { }
