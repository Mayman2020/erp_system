import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { FormArray, FormBuilder, Validators } from '@angular/forms';
import { forkJoin, Observable } from 'rxjs';
import { GoodsReceiptDto, GoodsReceiptForm, ProductDto, SupplierDto, WarehouseDto } from '../../core/models/erp.models';
import { AuthService } from '../../core/auth/auth.service';
import { ConfirmDialogService } from '../../core/services/confirm-dialog.service';
import { ErpApiService } from '../../core/services/erp-api.service';
import { DataTableAction, DataTableColumn } from '../../shared/components/data-table/data-table.component';
import { ErpMasterPageBase, MasterPageConfig, MASTER_CRUD_ACTIONS } from '../../shared/utils/erp-master-page.base';

@Component({
  standalone: false,
  selector: 'app-receipts-page',
  templateUrl: './receipts-page.component.html',
  styleUrls: ['./receipts-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ReceiptsPageComponent extends ErpMasterPageBase<GoodsReceiptDto, GoodsReceiptForm> implements OnInit, OnDestroy {
  readonly config: MasterPageConfig = {
    titleKey: 'MENU.GOODS_RECEIPTS',
    createKey: 'PURCHASES.CREATE_RECEIPT',
    editKey: 'COMMON.EDIT',
    viewKey: 'COMMON.VIEW',
    showStatus: true,
    statusOptions: ['DRAFT', 'APPROVED']
  };

  readonly columns: DataTableColumn[] = [
    { key: 'receiptNo', title: 'ERP.NUMBER' },
    { key: 'supplierName', title: 'MENU.SUPPLIERS' },
    { key: 'warehouseName', title: 'INVOICES.WAREHOUSE' },
    { key: 'status', title: 'COMMON.STATUS', kind: 'status' }
  ];

  readonly form = this.fb.group({
    receiptNo: [''],
    supplierId: [null as number | null],
    warehouseId: [null as number | null, Validators.required],
    purchaseOrderId: [null as number | null],
    notes: [''],
    lines: this.fb.array([this.createLineGroup()])
  });

  products: ProductDto[] = [];
  suppliers: SupplierDto[] = [];
  warehouses: WarehouseDto[] = [];

  constructor(
    private api: ErpApiService,
    private fb: FormBuilder,
    authService: AuthService,
    confirmDialog: ConfirmDialogService,
    cdr: ChangeDetectorRef
  ) {
    super(authService, confirmDialog, cdr);
  }

  get lines(): FormArray {
    return this.form.get('lines') as FormArray;
  }

  get tableActions(): DataTableAction[] {
    return [
      ...MASTER_CRUD_ACTIONS.slice(0, 2),
      { id: 'approve', labelKey: 'COMMON.APPROVE', className: 'erp-action-success', disabledWhen: (row) => String(row['status']) !== 'DRAFT' },
      MASTER_CRUD_ACTIONS[2]
    ];
  }

  get productLovItems() {
    return (this.products || []).map((p) => ({ id: p.id, label: `${p.code} - ${p.name || p.nameEn}` }));
  }

  get supplierLovItems() {
    return (this.suppliers || []).map((s) => ({ id: s.id, label: `${s.code} - ${s.nameEn}` }));
  }

  get warehouseLovItems() {
    return (this.warehouses || []).map((w) => ({ id: w.id, label: `${w.code} - ${w.nameEn || w.name}` }));
  }

  get productOptions() {
    return [{ id: null, label: '—' }, ...this.productLovItems];
  }

  get supplierOptions() {
    return [{ id: null, label: '—' }, ...this.supplierLovItems];
  }

  get warehouseOptions() {
    return [{ id: null, label: '—' }, ...this.warehouseLovItems];
  }

  ngOnInit(): void {
    forkJoin({ products: this.api.getProducts(), suppliers: this.api.getSuppliers(), warehouses: this.api.getWarehouses() }).subscribe({
      next: ({ products, suppliers, warehouses }) => {
        this.products = products || [];
        this.suppliers = suppliers || [];
        this.warehouses = warehouses || [];
        this.initMasterPage();
      }
    });
  }

  ngOnDestroy(): void { this.destroyMasterPage(); }

  addLine(): void {
    this.lines.push(this.createLineGroup());
    this.cdr.markForCheck();
  }

  removeLine(index: number): void {
    if (this.lines.length <= 1) return;
    this.lines.removeAt(index);
    this.cdr.markForCheck();
  }

  override onTableAction(event: { actionId: string; row: Record<string, unknown> }): void {
    const id = Number(event.row['id']);
    if (event.actionId === 'approve' && id) {
      this.api.approveGoodsReceipt(id).subscribe({
        next: () => { this.showSuccess('COMMON.APPROVE_SUCCESS'); this.load(); },
        error: (e) => this.showError(e?.error?.message || 'COMMON.UNEXPECTED_ERROR')
      });
      return;
    }
    super.onTableAction(event);
  }

  protected fetchList(_filters: Record<string, string>): Observable<GoodsReceiptDto[]> {
    return this.api.getGoodsReceipts();
  }

  protected fetchOne(id: number): Observable<GoodsReceiptDto> {
    return this.api.getGoodsReceipt(id);
  }

  protected createItem(payload: GoodsReceiptForm): Observable<GoodsReceiptDto> {
    return this.api.createGoodsReceipt(payload);
  }

  protected updateItem(id: number, payload: GoodsReceiptForm): Observable<GoodsReceiptDto> {
    return this.api.updateGoodsReceipt(id, payload);
  }

  protected removeItem(id: number): Observable<void> {
    return this.api.deleteGoodsReceipt(id);
  }

  protected defaultFormValues(): Record<string, unknown> {
    this.lines.clear();
    this.lines.push(this.createLineGroup());
    return { receiptNo: '', supplierId: null, warehouseId: null, purchaseOrderId: null, notes: '' };
  }

  protected patchForm(dto: GoodsReceiptDto): void {
    this.form.patchValue({
      receiptNo: dto.receiptNo,
      supplierId: dto.supplierId || null,
      warehouseId: dto.warehouseId,
      purchaseOrderId: dto.purchaseOrderId || null,
      notes: dto.notes || ''
    });
    this.lines.clear();
    (dto.lines || [{ productId: null, quantity: 1, unitCost: 0 }]).forEach((line) => {
      this.lines.push(this.fb.group({
        productId: [line.productId, Validators.required],
        quantity: [line.quantity, [Validators.required, Validators.min(0.0001)]],
        unitCost: [line.unitCost || 0, [Validators.min(0)]]
      }));
    });
  }

  protected toPayload(): GoodsReceiptForm {
    const v = this.form.getRawValue();
    return {
      receiptNo: v.receiptNo || undefined,
      supplierId: v.supplierId ? Number(v.supplierId) : undefined,
      warehouseId: Number(v.warehouseId),
      purchaseOrderId: v.purchaseOrderId ? Number(v.purchaseOrderId) : undefined,
      notes: v.notes || undefined,
      lines: (v.lines || []).map((line: Record<string, unknown>) => ({
        productId: Number(line['productId']),
        quantity: Number(line['quantity']),
        unitCost: Number(line['unitCost'] || 0)
      }))
    };
  }

  protected mapRow(dto: GoodsReceiptDto): Record<string, unknown> {
    return { ...dto };
  }

  private createLineGroup() {
    return this.fb.group({
      productId: [null as number | null, Validators.required],
      quantity: [1, [Validators.required, Validators.min(0.0001)]],
      unitCost: [0, [Validators.min(0)]]
    });
  }
}
