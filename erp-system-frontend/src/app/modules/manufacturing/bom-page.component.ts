import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { ProductBomLineDto, ProductBomLineForm, ProductDto } from '../../core/models/erp.models';
import { AuthService } from '../../core/auth/auth.service';
import { ConfirmDialogService } from '../../core/services/confirm-dialog.service';
import { ErpApiService } from '../../core/services/erp-api.service';
import { DataTableAction, DataTableColumn } from '../../shared/components/data-table/data-table.component';
import { MASTER_CRUD_ACTIONS } from '../../shared/utils/erp-master-page.base';

@Component({
  standalone: false,
  selector: 'app-bom-page',
  templateUrl: './bom-page.component.html',
  styleUrls: ['./bom-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class BomPageComponent implements OnInit, OnDestroy {
  readonly titleKey = 'MENU.BOM';
  readonly columns: DataTableColumn[] = [
    { key: 'componentProductCode', title: 'ERP.CODE' },
    { key: 'componentProductName', title: 'ERP.PRODUCT' },
    { key: 'quantityPerUnit', title: 'ERP.QUANTITY', align: 'end' }
  ];
  readonly actions: DataTableAction[] = MASTER_CRUD_ACTIONS;

  products: ProductDto[] = [];
  parentProductId: number | null = null;
  rows: Array<Record<string, unknown>> = [];
  loading = false;
  saving = false;
  formVisible = false;
  formMode: 'create' | 'edit' | 'view' = 'create';
  selectedId: number | null = null;
  errorKey = '';

  readonly form = this.fb.group({
    componentProductId: [null as number | null, Validators.required],
    quantityPerUnit: [1, [Validators.required, Validators.min(0.0001)]]
  });

  constructor(
    private api: ErpApiService,
    private fb: FormBuilder,
    private authService: AuthService,
    private confirmDialog: ConfirmDialogService,
    private cdr: ChangeDetectorRef
  ) {}

  get readOnly(): boolean {
    return this.formMode === 'view';
  }

  get productOptions(): Array<{ id: number | null; label: string }> {
    return [{ id: null, label: '—' }, ...this.products.map((p) => ({ id: p.id, label: `${p.code} - ${p.nameEn || p.name}` }))];
  }

  ngOnInit(): void {
    forkJoin({ products: this.api.getProducts() }).subscribe({
      next: ({ products }) => {
        this.products = products || [];
        this.cdr.markForCheck();
      }
    });
  }

  ngOnDestroy(): void {}

  onParentChange(productId: number | null): void {
    this.parentProductId = productId;
    this.loadLines();
  }

  loadLines(): void {
    if (!this.parentProductId) {
      this.rows = [];
      this.cdr.markForCheck();
      return;
    }
    this.loading = true;
    this.api.getProductBomLines(this.parentProductId).subscribe({
      next: (lines) => {
        this.rows = (lines || []).map((l) => ({ ...l, quantityPerUnit: Number(l.quantityPerUnit).toLocaleString(undefined, { minimumFractionDigits: 4 }) }));
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.loading = false;
        this.errorKey = 'COMMON.ERROR_LOADING';
        this.cdr.markForCheck();
      }
    });
  }

  openCreate(): void {
    if (!this.parentProductId) return;
    this.formMode = 'create';
    this.selectedId = null;
    this.form.reset({ componentProductId: null, quantityPerUnit: 1 });
    this.form.enable();
    this.formVisible = true;
    this.cdr.markForCheck();
  }

  closeForm(): void {
    this.formVisible = false;
    this.cdr.markForCheck();
  }

  onTableAction(event: { actionId: string; row: Record<string, unknown> }): void {
    const id = Number(event.row['id']);
    if (!id) return;
    if (event.actionId === 'view') this.openLine(id, 'view');
    else if (event.actionId === 'edit') this.openLine(id, 'edit');
    else if (event.actionId === 'delete') {
      this.confirmDialog.confirmByKey({ messageKey: 'COMMON.DELETE_CONFIRM' }).subscribe((ok) => {
        if (!ok) return;
        this.api.deleteProductBomLine(id).subscribe({ next: () => this.loadLines() });
      });
    }
  }

  openLine(id: number, mode: 'edit' | 'view'): void {
    this.api.getProductBomLine(id).subscribe({
      next: (dto) => {
        this.formMode = mode;
        this.selectedId = id;
        this.form.patchValue({ componentProductId: dto.componentProductId, quantityPerUnit: dto.quantityPerUnit });
        mode === 'view' ? this.form.disable() : this.form.enable();
        this.formVisible = true;
        this.cdr.markForCheck();
      }
    });
  }

  save(): void {
    if (!this.parentProductId || this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.getRawValue();
    const payload: ProductBomLineForm = {
      parentProductId: this.parentProductId,
      componentProductId: Number(v.componentProductId),
      quantityPerUnit: Number(v.quantityPerUnit)
    };
    this.saving = true;
    const req$ = this.formMode === 'edit' && this.selectedId
      ? this.api.updateProductBomLine(this.selectedId, payload)
      : this.api.createProductBomLine(payload);
    req$.subscribe({
      next: () => {
        this.saving = false;
        this.formVisible = false;
        this.loadLines();
      },
      error: () => {
        this.saving = false;
        this.errorKey = 'COMMON.ERROR_SAVING';
        this.cdr.markForCheck();
      }
    });
  }
}
