import { NgModule } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { HrRoutingModule } from './hr-routing.module';
import { DepartmentsPageComponent } from './departments-page.component';
import { EmployeesPageComponent } from './employees-page.component';
import { AttendancePageComponent } from './attendance-page.component';
import { LeaveRequestsPageComponent } from './leave-requests-page.component';
import { PayrollPageComponent } from './payroll-page.component';
import { DocumentsPageComponent } from './documents-page.component';
import { RecruitmentPageComponent } from './recruitment-page.component';

@NgModule({
  declarations: [DepartmentsPageComponent, EmployeesPageComponent, AttendancePageComponent, LeaveRequestsPageComponent, PayrollPageComponent, DocumentsPageComponent, RecruitmentPageComponent],
  imports: [SharedModule, HrRoutingModule]
})
export class HrModule {}
