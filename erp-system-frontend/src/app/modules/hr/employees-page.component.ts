import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Observable } from 'rxjs';
import { EmployeeDto, EmployeeForm, DepartmentDto } from '../../core/models/erp.models';
import { AuthService } from '../../core/auth/auth.service';
import { ConfirmDialogService } from '../../core/services/confirm-dialog.service';
import { ErpApiService } from '../../core/services/erp-api.service';
import { TranslationService } from '../../core/i18n/translation.service';
import { DataTableColumn } from '../../shared/components/data-table/data-table.component';
import { ExportColumn } from '../../shared/components/table-export-toolbar/table-export-toolbar.component';
import { ErpMasterPageBase, MasterPageConfig } from '../../shared/utils/erp-master-page.base';

@Component({
  standalone: false,
  selector: 'app-employees-page',
  templateUrl: './employees-page.component.html',
  styleUrls: ['./employees-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class EmployeesPageComponent extends ErpMasterPageBase<EmployeeDto, EmployeeForm> implements OnInit, OnDestroy {
  readonly config: MasterPageConfig = {
    titleKey: 'MENU.EMPLOYEES',
    createKey: 'ERP.CREATE_EMPLOYEE',
    editKey: 'COMMON.EDIT',
    viewKey: 'COMMON.VIEW'
  };

  readonly columns: DataTableColumn[] = [
    { key: 'employeeCode', title: 'ERP.CODE' },
    { key: 'fullNameEn', title: 'COMMON.NAME' },
    { key: 'jobTitle', title: 'ERP.JOB_TITLE' },
    { key: 'basicSalary', title: 'ERP.SALARY', align: 'end' },
    { key: 'active', title: 'COMMON.STATUS', kind: 'boolean' }
  ];

  readonly form = this.fb.group({ employeeCode: ['', Validators.required], fullNameEn: ['', Validators.required], fullNameAr: [''], email: [''], phone: [''], departmentId: [null], jobTitle: [''], hireDate: [''], basicSalary: [0, Validators.required], active: [true] });
  
  get tableActions() { return this.actions; }

  get activeCount(): number {
    return this.rows.filter((row) => !!row['active']).length;
  }

  get inactiveCount(): number {
    return Math.max(0, this.rows.length - this.activeCount);
  }

  constructor(
    private api: ErpApiService,
    private fb: FormBuilder,
    authService: AuthService,
    confirmDialog: ConfirmDialogService,
    public translationService: TranslationService,
    cdr: ChangeDetectorRef
  ) {
    super(authService, confirmDialog, cdr);
  }
  
  departments: DepartmentDto[] = [];
  selectedEmployee: EmployeeDto | null = null;

  readonly exportColumns: ExportColumn<EmployeeDto>[] = [
    { header: 'Code', value: 'employeeCode' },
    { header: 'Name', value: 'fullNameEn' },
    { header: 'Job Title', value: 'jobTitle' },
    { header: 'Salary', value: (row) => row.basicSalary ?? '' },
    { header: 'Active', value: (row) => row.active ? 'Yes' : 'No' }
  ];

  get departmentLovItems(): Array<{ id: number | null; label: string }> {
    return (this.departments || []).map((d) => ({ id: d.id, label: d.nameEn }));
  }
  ngOnInit(): void {
    this.initMasterPage();
    this.api.getDepartments().subscribe({ next: (rows) => { this.departments = rows || []; this.cdr.markForCheck(); } });
  }

  ngOnDestroy(): void {
    this.destroyMasterPage();
  }

  protected fetchList(filters: Record<string, string>): Observable<EmployeeDto[]> {
    return this.api.getEmployees(filters);
  }

  protected fetchOne(id: number): Observable<EmployeeDto> {
    return this.api.getEmployee(id);
  }

  protected createItem(payload: EmployeeForm): Observable<EmployeeDto> {
    return this.api.createEmployee(payload);
  }

  protected updateItem(id: number, payload: EmployeeForm): Observable<EmployeeDto> {
    return this.api.updateEmployee(id, payload);
  }

  protected removeItem(id: number): Observable<void> {
    return this.api.deleteEmployee(id);
  }

  protected defaultFormValues(): Record<string, unknown> {
    return this.form.getRawValue();
  }

  openCreate(): void {
    this.selectedEmployee = null;
    super.openCreate();
  }

  onTableAction(event: { actionId: string; row: Record<string, unknown> }): void {
    const id = Number(event.row['id']);
    if (id && (event.actionId === 'view' || event.actionId === 'edit')) {
      this.api.getEmployee(id).subscribe({
        next: (employee) => {
          this.selectedEmployee = employee;
          super.onTableAction(event);
        },
        error: () => super.onTableAction(event)
      });
      return;
    }
    super.onTableAction(event);
  }

  protected patchForm(dto: EmployeeDto): void {
    this.selectedEmployee = dto;
    this.form.patchValue({ employeeCode: dto.employeeCode, fullNameEn: dto.fullNameEn, fullNameAr: dto.fullNameAr || '', email: dto.email || '', phone: dto.phone || '', departmentId: dto.departmentId || null, jobTitle: dto.jobTitle || '', hireDate: dto.hireDate || '', basicSalary: dto.basicSalary || 0, active: dto.active !== false });
  }

  protected toPayload(): EmployeeForm {
    const v = this.form.getRawValue(); return { employeeCode: v.employeeCode, fullNameEn: v.fullNameEn, fullNameAr: v.fullNameAr || undefined, email: v.email || undefined, phone: v.phone || undefined, departmentId: v.departmentId || undefined, jobTitle: v.jobTitle || undefined, hireDate: v.hireDate || undefined, basicSalary: Number(v.basicSalary || 0), active: v.active !== false };
  }

  protected mapRow(dto: EmployeeDto): Record<string, unknown> {
    return { ...dto, basicSalary: dto.basicSalary != null ? Number(dto.basicSalary).toLocaleString(undefined, { minimumFractionDigits: 2 }) : '' };
  }
}
