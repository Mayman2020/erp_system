import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Observable } from 'rxjs';
import { UnitOfMeasureDto, UnitOfMeasureForm } from '../../core/models/erp.models';
import { AuthService } from '../../core/auth/auth.service';
import { ConfirmDialogService } from '../../core/services/confirm-dialog.service';
import { ErpApiService } from '../../core/services/erp-api.service';
import { DataTableAction, DataTableColumn } from '../../shared/components/data-table/data-table.component';
import { ErpMasterPageBase, MasterPageConfig, MASTER_CRUD_ACTIONS } from '../../shared/utils/erp-master-page.base';

@Component({
  standalone: false,
  selector: 'app-units-page',
  templateUrl: './units-page.component.html',
  styleUrls: ['./units-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class UnitsPageComponent extends ErpMasterPageBase<UnitOfMeasureDto, UnitOfMeasureForm> implements OnInit, OnDestroy {
  readonly config: MasterPageConfig = {
    titleKey: 'MENU.UNITS',
    createKey: 'ERP.CREATE_UNIT',
    editKey: 'COMMON.EDIT',
    viewKey: 'COMMON.VIEW'
  };

  readonly columns: DataTableColumn[] = [
    { key: 'code', title: 'ERP.CODE' },
    { key: 'nameEn', title: 'COMMON.NAME' },
    { key: 'active', title: 'COMMON.STATUS', kind: 'boolean' }
  ];

  readonly form = this.fb.group({ code: ['', Validators.required], nameEn: ['', Validators.required], nameAr: [''], active: [true] });
  
  get tableActions(): DataTableAction[] {
    return [
      ...MASTER_CRUD_ACTIONS,
      { id: 'activate', labelKey: 'COMMON.ACTIVATE', className: 'erp-action-success', disabledWhen: (r) => r['active'] !== false },
      { id: 'deactivate', labelKey: 'COMMON.DEACTIVATE', className: 'erp-action-warning', disabledWhen: (r) => r['active'] === false }
    ];
  }

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

  protected fetchList(filters: Record<string, string>): Observable<UnitOfMeasureDto[]> {
    return this.api.getUnits(filters);
  }

  protected fetchOne(id: number): Observable<UnitOfMeasureDto> {
    return this.api.getUnit(id);
  }

  protected createItem(payload: UnitOfMeasureForm): Observable<UnitOfMeasureDto> {
    return this.api.createUnit(payload);
  }

  protected updateItem(id: number, payload: UnitOfMeasureForm): Observable<UnitOfMeasureDto> {
    return this.api.updateUnit(id, payload);
  }

  protected removeItem(id: number): Observable<void> {
    return this.api.deleteUnit(id);
  }

  override onTableAction(event: { actionId: string; row: Record<string, unknown> }): void {
    const id = Number(event.row['id']);
    if (event.actionId === 'activate' && id) {
      this.api.activateUnit(id).subscribe({ next: () => { this.showSuccess('COMMON.ACTIVATE_SUCCESS'); this.load(); }, error: (e) => this.showError(e?.error?.message || 'COMMON.UNEXPECTED_ERROR') });
      return;
    }
    if (event.actionId === 'deactivate' && id) {
      this.api.deactivateUnit(id).subscribe({ next: () => { this.showSuccess('COMMON.DEACTIVATE_SUCCESS'); this.load(); }, error: (e) => this.showError(e?.error?.message || 'COMMON.UNEXPECTED_ERROR') });
      return;
    }
    super.onTableAction(event);
  }

  protected defaultFormValues(): Record<string, unknown> {
    return this.form.getRawValue();
  }

  protected patchForm(dto: UnitOfMeasureDto): void {
    this.form.patchValue({ code: dto.code, nameEn: dto.nameEn, nameAr: dto.nameAr || '', active: dto.active !== false });
  }

  protected toPayload(): UnitOfMeasureForm {
    const v = this.form.getRawValue(); return { code: v.code, nameEn: v.nameEn, nameAr: v.nameAr || undefined, active: v.active !== false };
  }

  protected mapRow(dto: UnitOfMeasureDto): Record<string, unknown> {
    return { ...dto };
  }
}
