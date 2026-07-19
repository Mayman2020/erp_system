import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Observable } from 'rxjs';
import { ProductCategoryDto, ProductCategoryForm } from '../../core/models/erp.models';
import { AuthService } from '../../core/auth/auth.service';
import { ConfirmDialogService } from '../../core/services/confirm-dialog.service';
import { ErpApiService } from '../../core/services/erp-api.service';
import { DataTableAction, DataTableColumn } from '../../shared/components/data-table/data-table.component';
import { ErpMasterPageBase, MasterPageConfig, MASTER_CRUD_ACTIONS } from '../../shared/utils/erp-master-page.base';

@Component({
  standalone: false,
  selector: 'app-categories-page',
  templateUrl: './categories-page.component.html',
  styleUrls: ['./categories-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CategoriesPageComponent extends ErpMasterPageBase<ProductCategoryDto, ProductCategoryForm> implements OnInit, OnDestroy {
  readonly config: MasterPageConfig = {
    titleKey: 'MENU.CATEGORIES',
    createKey: 'ERP.CREATE_CATEGORY',
    editKey: 'COMMON.EDIT',
    viewKey: 'COMMON.VIEW'
  };

  readonly columns: DataTableColumn[] = [
    { key: 'code', title: 'ERP.CODE' },
    { key: 'name', title: 'COMMON.NAME' },
    { key: 'parentCode', title: 'ERP.PARENT' },
    { key: 'active', title: 'COMMON.STATUS', kind: 'boolean' }
  ];

  readonly form = this.fb.group({ code: ['', Validators.required], nameEn: ['', Validators.required], nameAr: [''], parentId: [null], active: [true] });
  
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

  protected fetchList(filters: Record<string, string>): Observable<ProductCategoryDto[]> {
    return this.api.getCategories(filters);
  }

  protected fetchOne(id: number): Observable<ProductCategoryDto> {
    return this.api.getCategory(id);
  }

  protected createItem(payload: ProductCategoryForm): Observable<ProductCategoryDto> {
    return this.api.createCategory(payload);
  }

  protected updateItem(id: number, payload: ProductCategoryForm): Observable<ProductCategoryDto> {
    return this.api.updateCategory(id, payload);
  }

  protected removeItem(id: number): Observable<void> {
    return this.api.deleteCategory(id);
  }

  override onTableAction(event: { actionId: string; row: Record<string, unknown> }): void {
    const id = Number(event.row['id']);
    if (event.actionId === 'activate' && id) {
      this.api.activateCategory(id).subscribe({ next: () => { this.showSuccess('COMMON.ACTIVATE_SUCCESS'); this.load(); }, error: (e) => this.showError(e?.error?.message || 'COMMON.UNEXPECTED_ERROR') });
      return;
    }
    if (event.actionId === 'deactivate' && id) {
      this.api.deactivateCategory(id).subscribe({ next: () => { this.showSuccess('COMMON.DEACTIVATE_SUCCESS'); this.load(); }, error: (e) => this.showError(e?.error?.message || 'COMMON.UNEXPECTED_ERROR') });
      return;
    }
    super.onTableAction(event);
  }

  protected defaultFormValues(): Record<string, unknown> {
    return this.form.getRawValue();
  }

  protected patchForm(dto: ProductCategoryDto): void {
    this.form.patchValue({ code: dto.code, nameEn: dto.name || dto.nameEn, nameAr: dto.nameAr || '', parentId: dto.parentId || null, active: dto.active !== false });
  }

  protected toPayload(): ProductCategoryForm {
    const v = this.form.getRawValue(); return { code: v.code, nameEn: v.nameEn, nameAr: v.nameAr || undefined, parentId: v.parentId || undefined, active: v.active !== false };
  }

  protected mapRow(dto: ProductCategoryDto): Record<string, unknown> {
    return { ...dto, name: dto.name || dto.nameEn };
  }
}
