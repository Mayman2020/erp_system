import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Observable } from 'rxjs';
import { ProductCategoryDto, ProductCategoryForm } from '../../core/models/erp.models';
import { AuthService } from '../../core/auth/auth.service';
import { ConfirmDialogService } from '../../core/services/confirm-dialog.service';
import { ErpApiService } from '../../core/services/erp-api.service';
import { DataTableColumn } from '../../shared/components/data-table/data-table.component';
import { ErpMasterPageBase, MasterPageConfig } from '../../shared/utils/erp-master-page.base';

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
