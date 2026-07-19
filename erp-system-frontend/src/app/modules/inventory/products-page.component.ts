import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { forkJoin, Observable } from 'rxjs';
import { finalize } from 'rxjs/operators';
import { ProductCategoryDto, ProductDto, ProductForm, UnitOfMeasureDto } from '../../core/models/erp.models';
import { AuthService } from '../../core/auth/auth.service';
import { ConfirmDialogService } from '../../core/services/confirm-dialog.service';
import { ErpApiService } from '../../core/services/erp-api.service';
import { DataTableAction, DataTableColumn } from '../../shared/components/data-table/data-table.component';
import { ErpMasterPageBase, MasterPageConfig, MASTER_CRUD_ACTIONS } from '../../shared/utils/erp-master-page.base';

@Component({
  standalone: false,
  selector: 'app-products-page',
  templateUrl: './products-page.component.html',
  styleUrls: ['./products-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProductsPageComponent extends ErpMasterPageBase<ProductDto, ProductForm> implements OnInit, OnDestroy {
  readonly config: MasterPageConfig = {
    titleKey: 'MENU.PRODUCTS',
    createKey: 'ERP.CREATE_PRODUCT',
    editKey: 'COMMON.EDIT',
    viewKey: 'COMMON.VIEW'
  };

  readonly columns: DataTableColumn[] = [
    { key: 'code', title: 'ERP.CODE' },
    { key: 'name', title: 'COMMON.NAME' },
    { key: 'categoryName', title: 'MENU.CATEGORIES' },
    { key: 'salePrice', title: 'ERP.SALE_PRICE', align: 'end' },
    { key: 'totalQuantity', title: 'ERP.QUANTITY', align: 'end' },
    { key: 'active', title: 'COMMON.STATUS', kind: 'boolean' }
  ];

  readonly form = this.fb.group({
    code: ['', Validators.required],
    barcode: [''],
    nameEn: ['', Validators.required],
    nameAr: [''],
    categoryId: [null as number | null],
    unitId: [null as number | null, Validators.required],
    costPrice: [0],
    salePrice: [0],
    reorderLevel: [0],
    description: [''],
    active: [true]
  });

  categories: ProductCategoryDto[] = [];
  units: UnitOfMeasureDto[] = [];

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

  get categoryOptions(): Array<{ id: number | null; label: string }> {
    return [{ id: null, label: '—' }, ...(this.categories || []).map((c) => ({ id: c.id, label: `${c.code} - ${c.name || c.nameEn}` }))];
  }

  get unitOptions(): Array<{ id: number | null; label: string }> {
    return [{ id: null, label: '—' }, ...(this.units || []).map((u) => ({ id: u.id, label: `${u.code} - ${u.nameEn}` }))];
  }

  ngOnInit(): void {
    forkJoin({ categories: this.api.getCategories(), units: this.api.getUnits() })
      .subscribe({
        next: ({ categories, units }) => {
          this.categories = categories || [];
          this.units = units || [];
          this.initMasterPage();
        }
      });
  }

  ngOnDestroy(): void {
    this.destroyMasterPage();
  }

  protected fetchList(filters: Record<string, string>): Observable<ProductDto[]> {
    return this.api.getProducts(filters);
  }

  protected fetchOne(id: number): Observable<ProductDto> {
    return this.api.getProduct(id);
  }

  protected createItem(payload: ProductForm): Observable<ProductDto> {
    return this.api.createProduct(payload);
  }

  protected updateItem(id: number, payload: ProductForm): Observable<ProductDto> {
    return this.api.updateProduct(id, payload);
  }

  protected removeItem(id: number): Observable<void> {
    return this.api.deleteProduct(id);
  }

  override onTableAction(event: { actionId: string; row: Record<string, unknown> }): void {
    const id = Number(event.row['id']);
    if (event.actionId === 'activate' && id) {
      this.api.activateProduct(id).subscribe({ next: () => { this.showSuccess('COMMON.ACTIVATE_SUCCESS'); this.load(); }, error: (e) => this.showError(e?.error?.message || 'COMMON.UNEXPECTED_ERROR') });
      return;
    }
    if (event.actionId === 'deactivate' && id) {
      this.api.deactivateProduct(id).subscribe({ next: () => { this.showSuccess('COMMON.DEACTIVATE_SUCCESS'); this.load(); }, error: (e) => this.showError(e?.error?.message || 'COMMON.UNEXPECTED_ERROR') });
      return;
    }
    super.onTableAction(event);
  }

  protected defaultFormValues(): Record<string, unknown> {
    return {
      code: '', barcode: '', nameEn: '', nameAr: '', categoryId: null, unitId: null,
      costPrice: 0, salePrice: 0, reorderLevel: 0, description: '', active: true
    };
  }

  protected patchForm(dto: ProductDto): void {
    this.form.patchValue({
      code: dto.code,
      barcode: dto.barcode || '',
      nameEn: dto.name || dto.nameEn || '',
      nameAr: dto.nameAr || '',
      categoryId: dto.categoryId || null,
      unitId: dto.unitId || null,
      costPrice: dto.costPrice || 0,
      salePrice: dto.salePrice || 0,
      reorderLevel: dto.reorderLevel || 0,
      description: dto.description || '',
      active: dto.active !== false
    });
  }

  protected toPayload(): ProductForm {
    const v = this.form.getRawValue();
    return {
      code: v.code!,
      barcode: v.barcode || undefined,
      nameEn: v.nameEn!,
      nameAr: v.nameAr || undefined,
      categoryId: v.categoryId || undefined,
      unitId: Number(v.unitId),
      costPrice: Number(v.costPrice || 0),
      salePrice: Number(v.salePrice || 0),
      reorderLevel: Number(v.reorderLevel || 0),
      description: v.description || undefined,
      active: v.active !== false
    };
  }

  protected mapRow(dto: ProductDto): Record<string, unknown> {
    return {
      ...dto,
      name: dto.name || dto.nameEn,
      salePrice: dto.salePrice != null ? Number(dto.salePrice).toLocaleString(undefined, { minimumFractionDigits: 2 }) : '',
      totalQuantity: dto.totalQuantity != null ? Number(dto.totalQuantity).toLocaleString(undefined, { minimumFractionDigits: 2 }) : ''
    };
  }
}
