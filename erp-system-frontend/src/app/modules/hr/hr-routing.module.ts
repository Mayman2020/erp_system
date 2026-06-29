import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DepartmentsPageComponent } from './departments-page.component';
import { EmployeesPageComponent } from './employees-page.component';
import { AttendancePageComponent } from './attendance-page.component';
import { LeaveRequestsPageComponent } from './leave-requests-page.component';
import { PayrollPageComponent } from './payroll-page.component';
import { DocumentsPageComponent } from './documents-page.component';

const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'employees' },
  { path: 'departments', component: DepartmentsPageComponent },
  { path: 'employees', component: EmployeesPageComponent },
  { path: 'attendance', component: AttendancePageComponent },
  { path: 'leave-requests', component: LeaveRequestsPageComponent },
  { path: 'payroll', component: PayrollPageComponent },
  { path: 'documents', component: DocumentsPageComponent }
];

@NgModule({ imports: [RouterModule.forChild(routes)], exports: [RouterModule] })
export class HrRoutingModule {}
