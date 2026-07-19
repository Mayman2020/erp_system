import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { forkJoin, Subject } from 'rxjs';
import { finalize, takeUntil } from 'rxjs/operators';
import { ProductDto, WarehouseDto, WorkOrderDto, WorkOrderForm } from '../../core/models/erp.models';
import { AuthService } from '../../core/auth/auth.service';
import { ConfirmDialogService } from '../../core/services/confirm-dialog.service';
import { ErpApiService } from '../../core/services/erp-api.service';
import { DataTableAction, DataTableColumn } from '../../shared/components/data-table/data-table.component';

@Component({
  standalone: false,
  selector: 'app-manufacturing-page',
  templateUrl: './manufacturing-page.component.html',
  styleUrls: ['./manufacturing-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ManufacturingPageComponent implements OnInit, OnDestroy {
  readonly titleKey = 'MENU.WORK_ORDERS';
  readonly statusOptions = ['PLANNED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'];
  readonly columns: DataTableColumn[] = [
    { key: 'orderNumber', title: 'ERP.NUMBER' },
    { key: 'productName', title: 'ERP.PRODUCT', align: 'start' },
    { key: 'warehouseName', title: 'INVOICES.WAREHOUSE', align: 'start' },
    { key: 'quantity', title: 'ERP.QUANTITY', align: 'end' },
    { key: 'producedQuantity', title: 'MANUFACTURING.PRODUCED', align: 'end' },
    { key: 'plannedStart', title: 'MANUFACTURING.PLANNED_START', kind: 'date' },
    { key: 'plannedEnd', title: 'MANUFACTURING.PLANNED_END', kind: 'date' },
    { key: 'status', title: 'COMMON.STATUS', kind: 'status', prefix: 'WO_STATUS.' }
  ];
  readonly actions: DataTableAction[] = [
    { id: 'view', labelKey: 'COMMON.VIEW', className: 'erp-action-secondary' },
    { id: 'edit', labelKey: 'COMMON.EDIT', className: 'erp-action-info', disabledWhen: (row) => String(row['status']) !== 'PLANNED' },
    { id: 'start', labelKey: 'MANUFACTURING.START', className: 'erp-action-info', disabledWhen: (row) => String(row['status']) !== 'PLANNED' },
    { id: 'complete', labelKey: 'MANUFACTURING.COMPLETE', className: 'erp-action-success', disabledWhen: (row) => !['PLANNED', 'IN_PROGRESS'].includes(String(row['status'])) },
    { id: 'cancel', labelKey: 'COMMON.CANCEL', className: 'erp-action-warning', disabledWhen: (row) => ['COMPLETED', 'CANCELLED'].includes(String(row['status'])) },
    { id: 'delete', labelKey: 'COMMON.DELETE', className: 'erp-action-danger', disabledWhen: (row) => !['PLANNED', 'CANCELLED'].includes(String(row['status'])) }
  ];

  loading = false;
  saving = false;
  errorKey = '';
  successKey = '';
  rows: Array<Record<string, unknown>> = [];
  formVisible = false;
  formMode: 'create' | 'edit' | 'view' = 'create';
  selectedId: number | null = null;
  selectedAuditRecord: Record<string, unknown> | null = null;
  actorEmail = 'system@erp.local';

  products: ProductDto[] = [];
  warehouses: WarehouseDto[] = [];

  private readonly destroy$ = new Subject<void>();
  private feedbackTimer: ReturnType<typeof setTimeout> | null = null;

  readonly form = this.fb.group({
    orderNumber: [''],
    productId: [null as number | null, Validators.required],
    warehouseId: [null as number | null],
    quantity: [1, [Validators.required, Validators.min(0.0001)]],
    plannedStart: [''],
    plannedEnd: [''],
    notes: ['']
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
    return [
      { id: null, label: '—' },
      ...(this.products || []).map((p) => ({ id: p.id, label: `${p.code} - ${p.name || p.nameEn || ''}` }))
    ];
  }

  get warehouseOptions(): Array<{ id: number | null; label: string }> {
    return [
      { id: null, label: '—' },
      ...(this.warehouses || []).map((w) => ({ id: w.id, label: `${w.code} - ${w.nameEn || w.name || ''}` }))
    ];
  }

  ngOnInit(): void {
    this.authService.currentUser$.pipe(takeUntil(this.destroy$)).subscribe((user) => {
      this.actorEmail = user?.email || user?.username || 'system@erp.local';
      this.cdr.markForCheck();
    });
    this.authService.refreshCurrentUser();
    this.bootstrapLookups();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    if (this.feedbackTimer) {
      clearTimeout(this.feedbackTimer);
    }
  }

  openCreate(): void {
    this.formMode = 'create';
    this.selectedId = null;
    this.selectedAuditRecord = null;
    this.form.reset({
      orderNumber: '',
      productId: null,
      warehouseId: null,
      quantity: 1,
      plannedStart: new Date().toISOString().slice(0, 10),
      plannedEnd: '',
      notes: ''
    });
    this.form.enable();
    this.formVisible = true;
    this.errorKey = '';
    this.cdr.markForCheck();
  }

  closeForm(): void {
    this.formVisible = false;
    this.cdr.markForCheck();
  }

  onTableAction(event: { actionId: string; row: Record<string, unknown> }): void {
    const id = Number(event.row['id']);
    if (!id) {
      return;
    }
    if (event.actionId === 'view') {
      this.openDocument(id, 'view');
      return;
    }
    if (event.actionId === 'edit') {
      this.openDocument(id, 'edit');
      return;
    }
    if (event.actionId === 'start') {
      this.runAction(id, 'start');
      return;
    }
    if (event.actionId === 'complete') {
      this.runAction(id, 'complete');
      return;
    }
    if (event.actionId === 'cancel') {
      this.runAction(id, 'cancel');
      return;
    }
    if (event.actionId === 'delete') {
      this.confirmDelete(id);
    }
  }

  save(): void {
    if (this.readOnly || this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving = true;
    const payload = this.toPayload();
    const request$ = this.formMode === 'edit' && this.selectedId
      ? this.api.updateWorkOrder(this.selectedId, payload)
      : this.api.createWorkOrder(payload);
    request$
      .pipe(finalize(() => {
        this.saving = false;
        this.cdr.markForCheck();
      }))
      .subscribe({
        next: () => {
          this.showSuccess('MANUFACTURING.SAVE_SUCCESS');
          this.formVisible = false;
          this.load();
        },
        error: (err) => this.showError(err?.error?.message || 'COMMON.UNEXPECTED_ERROR')
      });
  }

  private bootstrapLookups(): void {
    this.loading = true;
    forkJoin({
      products: this.api.getProducts(),
      warehouses: this.api.getWarehouses()
    })
      .pipe(finalize(() => {
        this.loading = false;
        this.load();
        this.cdr.markForCheck();
      }))
      .subscribe({
        next: ({ products, warehouses }) => {
          this.products = products || [];
          this.warehouses = warehouses || [];
        },
        error: () => {
          this.errorKey = 'COMMON.ERROR_LOADING';
        }
      });
  }

  private load(): void {
    this.loading = true;
    this.api.getWorkOrders()
      .pipe(finalize(() => {
        this.loading = false;
        this.cdr.markForCheck();
      }))
      .subscribe({
        next: (rows) => {
          this.rows = (rows || []).map((row) => this.mapRow(row));
        },
        error: () => {
          this.errorKey = 'COMMON.ERROR_LOADING';
          this.rows = [];
        }
      });
  }

  private openDocument(id: number, mode: 'edit' | 'view'): void {
    this.api.getWorkOrder(id).subscribe({
      next: (order) => {
        this.formMode = mode;
        this.selectedId = order.id;
        this.selectedAuditRecord = order as unknown as Record<string, unknown>;
        this.form.reset({
          orderNumber: order.orderNumber,
          productId: order.productId,
          warehouseId: order.warehouseId || null,
          quantity: order.quantity,
          plannedStart: order.plannedStart || '',
          plannedEnd: order.plannedEnd || '',
          notes: order.notes || ''
        });
        if (mode === 'view') {
          this.form.disable();
        } else {
          this.form.enable();
        }
        this.formVisible = true;
        this.cdr.markForCheck();
      },
      error: () => this.showError('COMMON.ERROR_LOADING')
    });
  }

  private runAction(id: number, action: 'start' | 'complete' | 'cancel'): void {
    const messageKey = action === 'start'
      ? 'MANUFACTURING.START_CONFIRM'
      : action === 'complete'
        ? 'MANUFACTURING.COMPLETE_CONFIRM'
        : 'MANUFACTURING.CANCEL_CONFIRM';
    this.confirmDialog.confirmByKey({ messageKey, danger: action === 'cancel' }).subscribe((ok) => {
      if (!ok) {
        return;
      }
      const request$ = action === 'start'
        ? this.api.startWorkOrder(id, this.actorEmail)
        : action === 'complete'
          ? this.api.completeWorkOrder(id, this.actorEmail)
          : this.api.cancelWorkOrder(id, this.actorEmail);
      request$.subscribe({
        next: () => {
          this.showSuccess(`MANUFACTURING.${action.toUpperCase()}_SUCCESS`);
          this.formVisible = false;
          this.load();
        },
        error: (err) => this.showError(err?.error?.message || 'COMMON.UNEXPECTED_ERROR')
      });
    });
  }

  private confirmDelete(id: number): void {
    this.confirmDialog.confirmByKey({ messageKey: 'MANUFACTURING.DELETE_CONFIRM', danger: true }).subscribe((ok) => {
      if (!ok) {
        return;
      }
      this.api.deleteWorkOrder(id).subscribe({
        next: () => {
          this.showSuccess('MANUFACTURING.DELETE_SUCCESS');
          this.load();
        },
        error: (err) => this.showError(err?.error?.message || 'COMMON.UNEXPECTED_ERROR')
      });
    });
  }

  private toPayload(): WorkOrderForm {
    const raw = this.form.getRawValue();
    return {
      orderNumber: raw.orderNumber || undefined,
      productId: Number(raw.productId),
      warehouseId: raw.warehouseId ? Number(raw.warehouseId) : undefined,
      quantity: Number(raw.quantity),
      plannedStart: raw.plannedStart || undefined,
      plannedEnd: raw.plannedEnd || undefined,
      notes: raw.notes || undefined
    };
  }

  private mapRow(row: WorkOrderDto): Record<string, unknown> {
    const mapped: Record<string, unknown> = { ...row };
    ['quantity', 'producedQuantity'].forEach((key) => {
      const value = mapped[key];
      if (typeof value === 'number') {
        mapped[key] = Number(value).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 4 });
      }
    });
    return mapped;
  }

  private showError(key: string): void {
    this.errorKey = key;
    this.successKey = '';
    this.cdr.markForCheck();
  }

  private showSuccess(key: string): void {
    this.successKey = key;
    this.errorKey = '';
    if (this.feedbackTimer) {
      clearTimeout(this.feedbackTimer);
    }
    this.feedbackTimer = setTimeout(() => {
      this.successKey = '';
      this.cdr.markForCheck();
    }, 4000);
    this.cdr.markForCheck();
  }
}
