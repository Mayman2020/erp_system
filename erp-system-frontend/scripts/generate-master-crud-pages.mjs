#!/usr/bin/env node
/**
 * Generates full CRUD master-data pages from entity configs.
 */
import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const root = path.resolve(__dirname, '..');

const listPageScss = `.erp-list-page__card {
  margin-top: 0.5rem;
}

.erp-list-page__card .erp-card__body {
  padding: 1rem 1.25rem 1.25rem;
}

[data-theme='dark'] .erp-list-page__card {
  border-color: rgba(255, 255, 255, 0.08);
}
`;

const shellHtml = (titleKey, createKey, formFields, extraActions = '') => `<section class="erp-page erp-list-page erp-estate-fade-up">
  <app-page-header [titleKey]="titleKey">
    <div header-actions>
      <button class="erp-button erp-button--primary" type="button" (click)="openCreate()">
        <mat-icon aria-hidden="true">add</mat-icon>
        <span>{{ '${createKey}' | translate }}</span>
      </button>
    </div>
  </app-page-header>

  <div class="erp-card erp-list-page__card">
    <div class="erp-card__body">
      <app-advanced-search-bar
        *ngIf="showSearch"
        [showDateRange]="showDateRange"
        [showStatus]="showStatus"
        [statusOptions]="statusOptions"
        (search)="onSearch($event)"
      ></app-advanced-search-bar>

      <app-erp-alert *ngIf="errorKey && !formVisible" type="danger" [message]="errorKey" extraClass="mt-3" (dismissed)="errorKey = ''"></app-erp-alert>
      <app-erp-alert *ngIf="successKey && !formVisible" type="success" [message]="successKey" extraClass="mt-3" (dismissed)="successKey = ''"></app-erp-alert>

      <app-data-table [columns]="columns" [data]="rows" [actions]="tableActions" [loading]="loading" (actionClick)="onTableAction($event)"></app-data-table>
    </div>
  </div>
</section>

<app-erp-dialog
  [visible]="formVisible"
  [titleKey]="formMode === 'create' ? '${createKey}' : (formMode === 'edit' ? 'COMMON.EDIT' : 'COMMON.VIEW')"
  size="lg"
  (close)="closeForm()"
>
  <div dialog-body>
    <form [formGroup]="form" class="erp-dialog-form">
      <div class="row">
${formFields}
      </div>
    </form>
  </div>
  <div dialog-footer class="d-flex gap-2 justify-content-end">
    <button type="button" class="erp-button erp-button--secondary" (click)="closeForm()">{{ 'COMMON.CLOSE' | translate }}</button>
    <button *ngIf="!readOnly" type="button" class="erp-button erp-button--primary" [disabled]="saving" (click)="save()">{{ 'COMMON.SAVE' | translate }}</button>
  </div>
</app-erp-dialog>
`;

const field = (control, labelKey, type = 'text', extra = '') => `        <div class="col-md-6 mb-3">
          <app-form-field [control]="form.controls.${control}" labelKey="${labelKey}" icon="edit" type="${type}" [readonly]="readOnly" ${extra}></app-form-field>
        </div>`;

const selectField = (control, labelKey, optionsProp) => `        <div class="col-md-6 mb-3">
          <app-form-field [control]="form.controls.${control}" labelKey="${labelKey}" icon="list" type="select" [options]="${optionsProp}" optionValueField="id" optionLabelField="label" [disabled]="readOnly"></app-form-field>
        </div>`;

const entities = [
  {
    module: 'inventory',
    file: 'categories-page',
    className: 'CategoriesPageComponent',
    selector: 'app-categories-page',
    titleKey: 'MENU.CATEGORIES',
    createKey: 'ERP.CREATE_CATEGORY',
    dto: 'ProductCategoryDto',
    form: 'ProductCategoryForm',
    listMethod: 'getCategories',
    getMethod: 'getCategory',
    createMethod: 'createCategory',
    updateMethod: 'updateCategory',
    deleteMethod: 'deleteCategory',
    columns: `[
    { key: 'code', title: 'ERP.CODE' },
    { key: 'name', title: 'COMMON.NAME' },
    { key: 'parentCode', title: 'ERP.PARENT' },
    { key: 'active', title: 'COMMON.STATUS', kind: 'boolean' }
  ]`,
    formControls: `{ code: ['', Validators.required], nameEn: ['', Validators.required], nameAr: [''], parentId: [null], active: [true] }`,
    formFields: [field('code', 'ERP.CODE'), field('nameEn', 'COMMON.NAME'), field('nameAr', 'ERP.NAME_AR'), field('parentId', 'ERP.PARENT', 'number')],
    patch: `this.form.patchValue({ code: dto.code, nameEn: dto.name || dto.nameEn, nameAr: dto.nameAr || '', parentId: dto.parentId || null, active: dto.active !== false });`,
    payload: `const v = this.form.getRawValue(); return { code: v.code, nameEn: v.nameEn, nameAr: v.nameAr || undefined, parentId: v.parentId || undefined, active: v.active !== false };`,
    mapRow: `return { ...dto, name: dto.name || dto.nameEn };`
  },
  {
    module: 'inventory',
    file: 'warehouses-page',
    className: 'WarehousesPageComponent',
    selector: 'app-warehouses-page',
    titleKey: 'MENU.WAREHOUSES',
    createKey: 'ERP.CREATE_WAREHOUSE',
    dto: 'WarehouseDto',
    form: 'WarehouseForm',
    listMethod: 'getWarehouses',
    getMethod: 'getWarehouse',
    createMethod: 'createWarehouse',
    updateMethod: 'updateWarehouse',
    deleteMethod: 'deleteWarehouse',
    columns: `[
    { key: 'code', title: 'ERP.CODE' },
    { key: 'name', title: 'COMMON.NAME' },
    { key: 'location', title: 'ERP.LOCATION' },
    { key: 'active', title: 'COMMON.STATUS', kind: 'boolean' }
  ]`,
    formControls: `{ code: ['', Validators.required], nameEn: ['', Validators.required], nameAr: [''], location: [''], active: [true] }`,
    formFields: [field('code', 'ERP.CODE'), field('nameEn', 'COMMON.NAME'), field('nameAr', 'ERP.NAME_AR'), field('location', 'ERP.LOCATION')],
    patch: `this.form.patchValue({ code: dto.code, nameEn: dto.name || dto.nameEn, nameAr: dto.nameAr || '', location: dto.location || '', active: dto.active !== false });`,
    payload: `const v = this.form.getRawValue(); return { code: v.code, nameEn: v.nameEn, nameAr: v.nameAr || undefined, location: v.location || undefined, active: v.active !== false };`,
    mapRow: `return { ...dto, name: dto.name || dto.nameEn };`
  },
  {
    module: 'inventory',
    file: 'units-page',
    className: 'UnitsPageComponent',
    selector: 'app-units-page',
    titleKey: 'MENU.UNITS',
    createKey: 'ERP.CREATE_UNIT',
    dto: 'UnitOfMeasureDto',
    form: 'UnitOfMeasureForm',
    listMethod: 'getUnits',
    getMethod: 'getUnit',
    createMethod: 'createUnit',
    updateMethod: 'updateUnit',
    deleteMethod: 'deleteUnit',
    columns: `[
    { key: 'code', title: 'ERP.CODE' },
    { key: 'nameEn', title: 'COMMON.NAME' },
    { key: 'active', title: 'COMMON.STATUS', kind: 'boolean' }
  ]`,
    formControls: `{ code: ['', Validators.required], nameEn: ['', Validators.required], nameAr: [''], active: [true] }`,
    formFields: [field('code', 'ERP.CODE'), field('nameEn', 'COMMON.NAME'), field('nameAr', 'ERP.NAME_AR')],
    patch: `this.form.patchValue({ code: dto.code, nameEn: dto.nameEn, nameAr: dto.nameAr || '', active: dto.active !== false });`,
    payload: `const v = this.form.getRawValue(); return { code: v.code, nameEn: v.nameEn, nameAr: v.nameAr || undefined, active: v.active !== false };`,
    mapRow: `return { ...dto };`
  },
  {
    module: 'sales',
    file: 'customers-page',
    className: 'CustomersPageComponent',
    selector: 'app-customers-page',
    titleKey: 'MENU.CUSTOMERS',
    createKey: 'ERP.CREATE_CUSTOMER',
    dto: 'CustomerDto',
    form: 'CustomerForm',
    listMethod: 'getCustomers',
    getMethod: 'getCustomer',
    createMethod: 'createCustomer',
    updateMethod: 'updateCustomer',
    deleteMethod: 'deleteCustomer',
    columns: `[
    { key: 'code', title: 'ERP.CODE' },
    { key: 'nameEn', title: 'COMMON.NAME' },
    { key: 'email', title: 'ERP.EMAIL' },
    { key: 'phone', title: 'ERP.PHONE' },
    { key: 'active', title: 'COMMON.STATUS', kind: 'boolean' }
  ]`,
    formControls: `{ code: [''], nameEn: ['', Validators.required], nameAr: [''], email: [''], phone: [''], taxNumber: [''], address: [''], creditLimit: [0], active: [true] }`,
    formFields: [field('code', 'ERP.CODE'), field('nameEn', 'COMMON.NAME'), field('nameAr', 'ERP.NAME_AR'), field('email', 'ERP.EMAIL'), field('phone', 'ERP.PHONE'), field('taxNumber', 'ERP.TAX_NUMBER'), field('address', 'ERP.ADDRESS'), field('creditLimit', 'ERP.CREDIT_LIMIT', 'number', 'step="0.01" min="0"')],
    patch: `this.form.patchValue({ code: dto.code, nameEn: dto.nameEn, nameAr: dto.nameAr || '', email: dto.email || '', phone: dto.phone || '', taxNumber: dto.taxNumber || '', address: dto.address || '', creditLimit: dto.creditLimit || 0, active: dto.active !== false });`,
    payload: `const v = this.form.getRawValue(); return { code: v.code || undefined, nameEn: v.nameEn, nameAr: v.nameAr || undefined, email: v.email || undefined, phone: v.phone || undefined, taxNumber: v.taxNumber || undefined, address: v.address || undefined, creditLimit: Number(v.creditLimit || 0), active: v.active !== false };`,
    mapRow: `return { ...dto };`
  },
  {
    module: 'purchases',
    file: 'suppliers-page',
    className: 'SuppliersPageComponent',
    selector: 'app-suppliers-page',
    titleKey: 'MENU.SUPPLIERS',
    createKey: 'ERP.CREATE_SUPPLIER',
    dto: 'SupplierDto',
    form: 'SupplierForm',
    listMethod: 'getSuppliers',
    getMethod: 'getSupplier',
    createMethod: 'createSupplier',
    updateMethod: 'updateSupplier',
    deleteMethod: 'deleteSupplier',
    columns: `[
    { key: 'code', title: 'ERP.CODE' },
    { key: 'nameEn', title: 'COMMON.NAME' },
    { key: 'email', title: 'ERP.EMAIL' },
    { key: 'phone', title: 'ERP.PHONE' },
    { key: 'active', title: 'COMMON.STATUS', kind: 'boolean' }
  ]`,
    formControls: `{ code: [''], nameEn: ['', Validators.required], nameAr: [''], email: [''], phone: [''], taxNumber: [''], address: [''], active: [true] }`,
    formFields: [field('code', 'ERP.CODE'), field('nameEn', 'COMMON.NAME'), field('nameAr', 'ERP.NAME_AR'), field('email', 'ERP.EMAIL'), field('phone', 'ERP.PHONE'), field('taxNumber', 'ERP.TAX_NUMBER'), field('address', 'ERP.ADDRESS')],
    patch: `this.form.patchValue({ code: dto.code, nameEn: dto.nameEn, nameAr: dto.nameAr || '', email: dto.email || '', phone: dto.phone || '', taxNumber: dto.taxNumber || '', address: dto.address || '', active: dto.active !== false });`,
    payload: `const v = this.form.getRawValue(); return { code: v.code || undefined, nameEn: v.nameEn, nameAr: v.nameAr || undefined, email: v.email || undefined, phone: v.phone || undefined, taxNumber: v.taxNumber || undefined, address: v.address || undefined, active: v.active !== false };`,
    mapRow: `return { ...dto };`
  },
  {
    module: 'hr',
    file: 'departments-page',
    className: 'DepartmentsPageComponent',
    selector: 'app-departments-page',
    titleKey: 'MENU.DEPARTMENTS',
    createKey: 'ERP.CREATE_DEPARTMENT',
    dto: 'DepartmentDto',
    form: 'DepartmentForm',
    listMethod: 'getDepartments',
    getMethod: 'getDepartment',
    createMethod: 'createDepartment',
    updateMethod: 'updateDepartment',
    deleteMethod: 'deleteDepartment',
    columns: `[
    { key: 'code', title: 'ERP.CODE' },
    { key: 'nameEn', title: 'COMMON.NAME' },
    { key: 'active', title: 'COMMON.STATUS', kind: 'boolean' }
  ]`,
    formControls: `{ code: ['', Validators.required], nameEn: ['', Validators.required], nameAr: [''], managerId: [null], active: [true] }`,
    formFields: [field('code', 'ERP.CODE'), field('nameEn', 'COMMON.NAME'), field('nameAr', 'ERP.NAME_AR'), field('managerId', 'ERP.MANAGER', 'number')],
    patch: `this.form.patchValue({ code: dto.code, nameEn: dto.nameEn, nameAr: dto.nameAr || '', managerId: dto.managerId || null, active: dto.active !== false });`,
    payload: `const v = this.form.getRawValue(); return { code: v.code, nameEn: v.nameEn, nameAr: v.nameAr || undefined, managerId: v.managerId || undefined, active: v.active !== false };`,
    mapRow: `return { ...dto };`
  },
  {
    module: 'hr',
    file: 'employees-page',
    className: 'EmployeesPageComponent',
    selector: 'app-employees-page',
    titleKey: 'MENU.EMPLOYEES',
    createKey: 'ERP.CREATE_EMPLOYEE',
    dto: 'EmployeeDto',
    form: 'EmployeeForm',
    listMethod: 'getEmployees',
    getMethod: 'getEmployee',
    createMethod: 'createEmployee',
    updateMethod: 'updateEmployee',
    deleteMethod: 'deleteEmployee',
    columns: `[
    { key: 'employeeCode', title: 'ERP.CODE' },
    { key: 'fullNameEn', title: 'COMMON.NAME' },
    { key: 'jobTitle', title: 'ERP.JOB_TITLE' },
    { key: 'basicSalary', title: 'ERP.SALARY', align: 'end' },
    { key: 'active', title: 'COMMON.STATUS', kind: 'boolean' }
  ]`,
    formControls: `{ employeeCode: ['', Validators.required], fullNameEn: ['', Validators.required], fullNameAr: [''], email: [''], phone: [''], departmentId: [null], jobTitle: [''], hireDate: [''], basicSalary: [0, Validators.required], active: [true] }`,
    formFields: [field('employeeCode', 'ERP.CODE'), field('fullNameEn', 'COMMON.NAME'), field('fullNameAr', 'ERP.NAME_AR'), field('email', 'ERP.EMAIL'), field('phone', 'ERP.PHONE'), selectField('departmentId', 'MENU.DEPARTMENTS', 'departmentOptions'), field('jobTitle', 'ERP.JOB_TITLE'), field('hireDate', 'ERP.HIRE_DATE', 'date'), field('basicSalary', 'ERP.SALARY', 'number', 'step="0.01" min="0"')],
    patch: `this.form.patchValue({ employeeCode: dto.employeeCode, fullNameEn: dto.fullNameEn, fullNameAr: dto.fullNameAr || '', email: dto.email || '', phone: dto.phone || '', departmentId: dto.departmentId || null, jobTitle: dto.jobTitle || '', hireDate: dto.hireDate || '', basicSalary: dto.basicSalary || 0, active: dto.active !== false });`,
    payload: `const v = this.form.getRawValue(); return { employeeCode: v.employeeCode, fullNameEn: v.fullNameEn, fullNameAr: v.fullNameAr || undefined, email: v.email || undefined, phone: v.phone || undefined, departmentId: v.departmentId || undefined, jobTitle: v.jobTitle || undefined, hireDate: v.hireDate || undefined, basicSalary: Number(v.basicSalary || 0), active: v.active !== false };`,
    mapRow: `return { ...dto, basicSalary: dto.basicSalary != null ? Number(dto.basicSalary).toLocaleString(undefined, { minimumFractionDigits: 2 }) : '' };`,
    extraTs: `
  departments: DepartmentDto[] = [];
  get departmentOptions(): Array<{ id: number | null; label: string }> {
    return [{ id: null, label: '—' }, ...(this.departments || []).map((d) => ({ id: d.id, label: d.nameEn }))];
  }
  override ngOnInit(): void {
    super.ngOnInit();
    this.api.getDepartments().subscribe({ next: (rows) => { this.departments = rows || []; this.cdr.markForCheck(); } });
  }`,
    extraImports: ', DepartmentDto'
  }
];

function generateTs(e) {
  const extraImports = e.extraImports || '';
  const extraTs = e.extraTs || '';
  const workflowActions = e.workflowActions ? `override readonly actions = ${e.workflowActions};` : '';
  const tableActionsGetter = e.workflowActions ? `get tableActions() { return this.actions; }` : `get tableActions() { return this.actions; }`;

  return `import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Observable } from 'rxjs';
import { ${e.dto}, ${e.form}${extraImports} } from '../../core/models/erp.models';
import { AuthService } from '../../core/auth/auth.service';
import { ConfirmDialogService } from '../../core/services/confirm-dialog.service';
import { ErpApiService } from '../../core/services/erp-api.service';
import { DataTableColumn } from '../../shared/components/data-table/data-table.component';
import { ErpMasterPageBase, MasterPageConfig } from '../../shared/utils/erp-master-page.base';

@Component({
  standalone: false,
  selector: '${e.selector}',
  templateUrl: './${e.file}.component.html',
  styleUrls: ['./${e.file}.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ${e.className} extends ErpMasterPageBase<${e.dto}, ${e.form}> implements OnInit, OnDestroy {
  readonly config: MasterPageConfig = {
    titleKey: '${e.titleKey}',
    createKey: '${e.createKey}',
    editKey: 'COMMON.EDIT',
    viewKey: 'COMMON.VIEW'
  };

  readonly columns: DataTableColumn[] = ${e.columns};

  readonly form = this.fb.group(${e.formControls});
  ${workflowActions}
  ${tableActionsGetter}

  constructor(
    private api: ErpApiService,
    private fb: FormBuilder,
    authService: AuthService,
    confirmDialog: ConfirmDialogService,
    cdr: ChangeDetectorRef
  ) {
    super(authService, confirmDialog, cdr);
  }
  ${extraTs}

  ngOnInit(): void { this.initMasterPage(); }
  ngOnDestroy(): void { this.destroyMasterPage(); }

  protected fetchList(filters: Record<string, string>): Observable<${e.dto}[]> {
    return this.api.${e.listMethod}(filters);
  }

  protected fetchOne(id: number): Observable<${e.dto}> {
    return this.api.${e.getMethod}(id);
  }

  protected createItem(payload: ${e.form}): Observable<${e.dto}> {
    return this.api.${e.createMethod}(payload);
  }

  protected updateItem(id: number, payload: ${e.form}): Observable<${e.dto}> {
    return this.api.${e.updateMethod}(id, payload);
  }

  protected removeItem(id: number): Observable<void> {
    return this.api.${e.deleteMethod}(id);
  }

  protected defaultFormValues(): Record<string, unknown> {
    return this.form.getRawValue();
  }

  protected patchForm(dto: ${e.dto}): void {
    ${e.patch}
  }

  protected toPayload(): ${e.form} {
    ${e.payload}
  }

  protected mapRow(dto: ${e.dto}): Record<string, unknown> {
    ${e.mapRow}
  }
}
`;
}

for (const e of entities) {
  const dir = path.join(root, 'src', 'app', 'modules', e.module);
  fs.writeFileSync(path.join(dir, `${e.file}.component.ts`), generateTs(e));
  fs.writeFileSync(path.join(dir, `${e.file}.component.html`), shellHtml(e.titleKey, e.createKey, e.formFields.join('\n')));
  fs.writeFileSync(path.join(dir, `${e.file}.component.scss`), listPageScss);
  console.log('Generated', e.module, e.file);
}

console.log('Done.');
