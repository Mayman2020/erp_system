import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { PermissionGuard } from '../../core/auth/permission.guard';
import { DepartmentsPageComponent } from './departments-page.component';
import { EmployeesPageComponent } from './employees-page.component';
import { AttendancePageComponent } from './attendance-page.component';
import { LeaveRequestsPageComponent } from './leave-requests-page.component';
import { PayrollPageComponent } from './payroll-page.component';
import { DocumentsPageComponent } from './documents-page.component';
import { RecruitmentPageComponent } from './recruitment-page.component';

const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'employees' },
  { path: 'departments', canActivate: [PermissionGuard], data: { menuItemId: 'erp-hr-departments' }, component: DepartmentsPageComponent },
  { path: 'employees', canActivate: [PermissionGuard], data: { menuItemId: 'erp-hr-employees' }, component: EmployeesPageComponent },
  { path: 'attendance', canActivate: [PermissionGuard], data: { menuItemId: 'erp-hr-attendance' }, component: AttendancePageComponent },
  { path: 'leave-requests', canActivate: [PermissionGuard], data: { menuItemId: 'erp-hr-leave' }, component: LeaveRequestsPageComponent },
  { path: 'payroll', canActivate: [PermissionGuard], data: { menuItemId: 'erp-hr-payroll' }, component: PayrollPageComponent },
  { path: 'documents', canActivate: [PermissionGuard], data: { menuItemId: 'erp-hr-documents' }, component: DocumentsPageComponent },
  { path: 'recruitment', canActivate: [PermissionGuard], data: { menuItemId: 'erp-hr-recruitment' }, component: RecruitmentPageComponent }
];

@NgModule({ imports: [RouterModule.forChild(routes)], exports: [RouterModule] })
export class HrRoutingModule {}
