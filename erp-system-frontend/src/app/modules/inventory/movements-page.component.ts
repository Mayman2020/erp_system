import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { forkJoin, Observable } from 'rxjs';
import { finalize } from 'rxjs/operators';
import { ProductDto, StockMovementDto, StockMovementForm, WarehouseDto } from '../../core/models/erp.models';
import { AuthService } from '../../core/auth/auth.service';
import { ConfirmDialogService } from '../../core/services/confirm-dialog.service';
import { ErpApiService } from '../../core/services/erp-api.service';
import { DataTableAction, DataTableColumn } from '../../shared/components/data-table/data-table.component';
import { ErpMasterPageBase, MasterPageConfig, MASTER_CRUD_ACTIONS } from '../../shared/utils/erp-master-page.base';

@Component({
  standalone: false,
  selector: 'app-movements-page',
  templateUrl: './movements-page.component.html',
  styleUrls: ['./movements-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class MovementsPageComponent extends ErpMasterPageBase<StockMovementDto, StockMovementForm> implements OnInit, OnDestroy {
  readonly config: MasterPageConfig = {
    titleKey: 'MENU.STOCK_MOVEMENTS',
    createKey: 'ERP.CREATE_MOVEMENT',
    editKey: 'COMMON.EDIT',
    viewKey: 'COMMON.VIEW',
    showStatus: true,
    statusOptions: ['DRAFT', 'PENDING', 'APPROVED', 'CANCELLED']
  };

  readonly columns: DataTableColumn[] = [
    { key: 'movementNumber', title: 'ERP.NUMBER' },
    { key: 'movementDate', title: 'COMMON.DATE', kind: 'date' },
    { key: 'movementType', title: 'ERP.MOVEMENT_TYPE' },
    { key: 'productName', title: 'ERP.PRODUCT' },
    { key: 'warehouseName', title: 'INVOICES.WAREHOUSE' },
    { key: 'quantity', title: 'ERP.QUANTITY', align: 'end' },
    { key: 'status', title: 'COMMON.STATUS', kind: 'status' }
  ];

  readonly movementTypes = ['IN', 'OUT', 'TRANSFER', 'ADJUSTMENT'];

  readonly form = this.fb.group({
    movementNumber: [''],
    movementDate: [new Date().toISOString().slice(0, 10), Validators.required],
    movementType: ['IN', Validators.required],
    productId: [null as number | null, Validators.required],
    warehouseId: [null as number | null, Validators.required],
    targetWarehouseId: [null as number | null],
    quantity: [1, [Validators.required, Validators.min(0.0001)]],
    unitCost: [0],
    notes: ['']
  });

  products: ProductDto[] = [];
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

  get tableActions(): DataTableAction[] {
    return [
      ...MASTER_CRUD_ACTIONS.slice(0, 2),
      { id: 'submit', labelKey: 'ERP.SUBMIT', className: 'erp-action-info', disabledWhen: (row) => String(row['status']) !== 'DRAFT' },
      { id: 'approve', labelKey: 'COMMON.APPROVE', className: 'erp-action-success', disabledWhen: (row) => !['DRAFT', 'PENDING'].includes(String(row['status'])) },
      { id: 'cancel', labelKey: 'COMMON.CANCEL', className: 'erp-action-warning', disabledWhen: (row) => ['APPROVED', 'CANCELLED'].includes(String(row['status'])) },
      MASTER_CRUD_ACTIONS[2]
    ];
  }

  get productOptions() {
    return [{ id: null, label: '—' }, ...(this.products || []).map((p) => ({ id: p.id, label: `${p.code} - ${p.name || p.nameEn}` }))];
  }

  get warehouseOptions() {
    return [{ id: null, label: '—' }, ...(this.warehouses || []).map((w) => ({ id: w.id, label: `${w.code} - ${w.nameEn || w.name}` }))];
  }

  get movementTypeOptions() {
    return this.movementTypes.map((t) => ({ id: t, label: t }));
  }

  ngOnInit(): void {
    forkJoin({ products: this.api.getProducts(), warehouses: this.api.getWarehouses() }).subscribe({
      next: ({ products, warehouses }) => {
        this.products = products || [];
        this.warehouses = warehouses || [];
        this.initMasterPage();
      }
    });
  }

  ngOnDestroy(): void { this.destroyMasterPage(); }

  override onTableAction(event: { actionId: string; row: Record<string, unknown> }): void {
    const id = Number(event.row['id']);
    if (event.actionId === 'submit' && id) {
      this.api.submitStockMovement(id).subscribe({ next: () => { this.showSuccess('ERP.SUBMIT_SUCCESS'); this.load(); }, error: (e) => this.showError(e?.error?.message || 'COMMON.UNEXPECTED_ERROR') });
      return;
    }
    if (event.actionId === 'approve' && id) {
      this.api.approveStockMovement(id).subscribe({ next: () => { this.showSuccess('COMMON.APPROVE_SUCCESS'); this.load(); }, error: (e) => this.showError(e?.error?.message || 'COMMON.UNEXPECTED_ERROR') });
      return;
    }
    if (event.actionId === 'cancel' && id) {
      this.api.cancelStockMovement(id).subscribe({ next: () => { this.showSuccess('COMMON.CANCEL_SUCCESS'); this.load(); }, error: (e) => this.showError(e?.error?.message || 'COMMON.UNEXPECTED_ERROR') });
      return;
    }
    super.onTableAction(event);
  }

  protected fetchList(filters: Record<string, string>): Observable<StockMovementDto[]> {
    return this.api.getStockMovements(filters);
  }

  protected fetchOne(id: number): Observable<StockMovementDto> {
    return this.api.getStockMovement(id);
  }

  protected createItem(payload: StockMovementForm): Observable<StockMovementDto> {
    return this.api.createStockMovement(payload);
  }

  protected updateItem(id: number, payload: StockMovementForm): Observable<StockMovementDto> {
    return this.api.updateStockMovement(id, payload);
  }

  protected removeItem(id: number): Observable<void> {
    return new Observable((observer) => {
      this.api.cancelStockMovement(id).subscribe({
        next: () => { observer.next(); observer.complete(); },
        error: (err) => observer.error(err)
      });
    });
  }

  protected defaultFormValues(): Record<string, unknown> {
    return { movementNumber: '', movementDate: new Date().toISOString().slice(0, 10), movementType: 'IN', productId: null, warehouseId: null, targetWarehouseId: null, quantity: 1, unitCost: 0, notes: '' };
  }

  protected patchForm(dto: StockMovementDto): void {
    this.form.patchValue({
      movementNumber: dto.movementNumber, movementDate: dto.movementDate, movementType: dto.movementType,
      productId: dto.productId, warehouseId: dto.warehouseId, targetWarehouseId: dto.targetWarehouseId || null,
      quantity: dto.quantity, unitCost: dto.unitCost || 0, notes: dto.notes || ''
    });
  }

  protected toPayload(): StockMovementForm {
    const v = this.form.getRawValue();
    return {
      movementNumber: v.movementNumber || undefined,
      movementDate: v.movementDate!,
      movementType: v.movementType!,
      productId: Number(v.productId),
      warehouseId: Number(v.warehouseId),
      targetWarehouseId: v.targetWarehouseId ? Number(v.targetWarehouseId) : undefined,
      quantity: Number(v.quantity),
      unitCost: Number(v.unitCost || 0),
      notes: v.notes || undefined
    };
  }

  protected mapRow(dto: StockMovementDto): Record<string, unknown> {
    return { ...dto, quantity: Number(dto.quantity).toLocaleString(undefined, { minimumFractionDigits: 2 }) };
  }
}
