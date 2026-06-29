import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Observable } from 'rxjs';
import { DepartmentDto, DepartmentForm } from '../../core/models/erp.models';
import { AuthService } from '../../core/auth/auth.service';
import { ConfirmDialogService } from '../../core/services/confirm-dialog.service';
import { ErpApiService } from '../../core/services/erp-api.service';
import { DataTableColumn } from '../../shared/components/data-table/data-table.component';
import { ErpMasterPageBase, MasterPageConfig } from '../../shared/utils/erp-master-page.base';

@Component({
  standalone: false,
  selector: 'app-departments-page',
  templateUrl: './departments-page.component.html',
  styleUrls: ['./departments-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DepartmentsPageComponent extends ErpMasterPageBase<DepartmentDto, DepartmentForm> implements OnInit, OnDestroy {
  readonly config: MasterPageConfig = {
    titleKey: 'MENU.DEPARTMENTS',
    createKey: 'ERP.CREATE_DEPARTMENT',
    editKey: 'COMMON.EDIT',
    viewKey: 'COMMON.VIEW'
  };

  readonly columns: DataTableColumn[] = [
    { key: 'code', title: 'ERP.CODE' },
    { key: 'nameEn', title: 'COMMON.NAME' },
    { key: 'active', title: 'COMMON.STATUS', kind: 'boolean' }
  ];

  readonly form = this.fb.group({ code: ['', Validators.required], nameEn: ['', Validators.required], nameAr: [''], managerId: [null], active: [true] });
  
  get tableActions() { return this.actions; }

  constructor(
    private api: ErpApiService,
    private fb: FormBuilder,
    authService: AuthService,
    confirmDialog: ConfirmDialogService,
    cdr: ChangeDetectorRef
  ) {
    super(authService, confirmDialog, cdr);
  }
  

  ngOnInit(): void { this.initMasterPage(); }
  ngOnDestroy(): void { this.destroyMasterPage(); }

  protected fetchList(filters: Record<string, string>): Observable<DepartmentDto[]> {
    return this.api.getDepartments(filters);
  }

  protected fetchOne(id: number): Observable<DepartmentDto> {
    return this.api.getDepartment(id);
  }

  protected createItem(payload: DepartmentForm): Observable<DepartmentDto> {
    return this.api.createDepartment(payload);
  }

  protected updateItem(id: number, payload: DepartmentForm): Observable<DepartmentDto> {
    return this.api.updateDepartment(id, payload);
  }

  protected removeItem(id: number): Observable<void> {
    return this.api.deleteDepartment(id);
  }

  protected defaultFormValues(): Record<string, unknown> {
    return this.form.getRawValue();
  }

  protected patchForm(dto: DepartmentDto): void {
    this.form.patchValue({ code: dto.code, nameEn: dto.nameEn, nameAr: dto.nameAr || '', managerId: dto.managerId || null, active: dto.active !== false });
  }

  protected toPayload(): DepartmentForm {
    const v = this.form.getRawValue(); return { code: v.code, nameEn: v.nameEn, nameAr: v.nameAr || undefined, managerId: v.managerId || undefined, active: v.active !== false };
  }

  protected mapRow(dto: DepartmentDto): Record<string, unknown> {
    return { ...dto };
  }
}
