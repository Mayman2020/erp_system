import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { forkJoin, Observable } from 'rxjs';
import { finalize } from 'rxjs/operators';
import { PurchaseOrderDto, PurchaseOrderForm } from '../../core/models/erp.models';
import { AuthService } from '../../core/auth/auth.service';
import { ConfirmDialogService } from '../../core/services/confirm-dialog.service';
import { ErpApiService } from '../../core/services/erp-api.service';
import { DataTableAction, DataTableColumn } from '../../shared/components/data-table/data-table.component';
import { DocumentPageConfig, ErpDocumentPageBase } from '../../shared/utils/erp-document-page.base';
import { DOCUMENT_ACTIONS, mapAmountRow } from '../../shared/utils/erp-document.utils';

@Component({
  standalone: false,
  selector: 'app-orders-page',
  templateUrl: './orders-page.component.html',
  styleUrls: ['./orders-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class OrdersPageComponent extends ErpDocumentPageBase<PurchaseOrderDto, PurchaseOrderForm> implements OnInit, OnDestroy {
  readonly config: DocumentPageConfig = {
    titleKey: 'MENU.PURCHASE_ORDERS',
    numberKey: 'ERP.NUMBER',
    dateKey: 'ERP.DATE',
    partyKey: 'MENU.SUPPLIERS',
    partyField: 'supplierId',
    numberField: 'orderNumber',
    dateField: 'orderDate',
    mode: 'full',
    priceField: 'costPrice',
    showWarehouse: true
  };

  readonly columns: DataTableColumn[] = [
    { key: 'orderNumber', title: 'ERP.NUMBER' },
    { key: 'orderDate', title: 'ERP.DATE', kind: 'date' },
    { key: 'supplierName', title: 'MENU.SUPPLIERS', align: 'start' },
    { key: 'totalAmount', title: 'ERP.TOTAL', align: 'end' },
    { key: 'status', title: 'COMMON.STATUS', kind: 'status', prefix: 'STATUS.' }
  ];

  readonly form = this.fb.group({
    orderNumber: [''],
    orderDate: ['', Validators.required],
    supplierId: [null as number | null, Validators.required],
    warehouseId: [null as number | null],
    discountAmount: [0],
    notes: [''],
    lines: this.fb.array([])
  });

  constructor(
    private api: ErpApiService,
    fb: FormBuilder,
    confirmDialog: ConfirmDialogService,
    private authService: AuthService,
    cdr: ChangeDetectorRef
  ) {
    super(fb, confirmDialog, cdr);
  }

  ngOnInit(): void {
    this.authService.refreshCurrentUser();
    this.initActor(() => this.bootstrap(), this.authService.currentUser$);
  }

  ngOnDestroy(): void {
    this.destroy();
  }

  override actions: DataTableAction[] = [
    ...DOCUMENT_ACTIONS.slice(0, 3),
    { id: 'convert', labelKey: 'ERP.CONVERT_TO_INVOICE', className: 'erp-action-success', disabledWhen: (r) => String(r['status']) !== 'APPROVED' },
    ...DOCUMENT_ACTIONS.slice(3)
  ];

  override onTableAction(event: { actionId: string; row: Record<string, unknown> }): void {
    const id = Number(event.row['id']);
    if (event.actionId === 'convert' && id) {
      this.confirmDialog.confirmByKey({ messageKey: 'ERP.CONVERT_TO_INVOICE_CONFIRM' }).subscribe((ok) => {
        if (!ok) return;
        this.api.convertPurchaseOrderToInvoice(id, this.actorEmail).subscribe({
          next: () => {
            this.showSuccess('ERP.CONVERT_TO_INVOICE_SUCCESS');
            this.reloadList();
          },
          error: () => this.showError('COMMON.ERROR_SAVING')
        });
      });
      return;
    }
    super.onTableAction(event);
  }

  protected reloadList(): void {
    this.loading = true;
    this.api.getPurchaseOrders().pipe(finalize(() => {
      this.loading = false;
      this.cdr.markForCheck();
    })).subscribe({
      next: (rows) => {
        this.rows = (rows || []).map((r) => {
          const mapped = mapAmountRow(r as unknown as Record<string, unknown>);
          const supplier = this.parties.find((p) => p.id === r.supplierId);
          if (supplier) {
            mapped['supplierName'] = supplier.label;
          }
          return mapped;
        });
      },
      error: () => { this.errorKey = 'COMMON.ERROR_LOADING'; this.rows = []; }
    });
  }

  protected fetchById(id: number): Observable<PurchaseOrderDto> { return this.api.getPurchaseOrder(id); }
  protected createRequest(payload: PurchaseOrderForm): Observable<PurchaseOrderDto> { return this.api.createPurchaseOrder(payload); }
  protected updateRequest(id: number, payload: PurchaseOrderForm): Observable<PurchaseOrderDto> { return this.api.updatePurchaseOrder(id, payload); }
  protected deleteRequest(id: number): Observable<void> { return this.api.deletePurchaseOrder(id); }
  protected approveRequest(id: number, actor: string): Observable<PurchaseOrderDto> { return this.api.approvePurchaseOrder(id, actor); }
  protected cancelRequest(id: number, actor: string): Observable<PurchaseOrderDto> { return this.api.cancelPurchaseOrder(id, actor); }

  protected buildPayload(): PurchaseOrderForm {
    const raw = this.form.getRawValue();
    return {
      orderNumber: raw.orderNumber || undefined,
      orderDate: raw.orderDate,
      supplierId: Number(raw.supplierId),
      warehouseId: raw.warehouseId ? Number(raw.warehouseId) : undefined,
      discountAmount: Number(raw.discountAmount || 0),
      notes: raw.notes || undefined,
      lines: (raw.lines || []).map((line: Record<string, unknown>) => ({
        productId: Number(line['productId']),
        description: String(line['description'] || '') || undefined,
        quantity: Number(line['quantity']),
        unitPrice: Number(line['unitPrice']),
        discountPercent: Number(line['discountPercent'] || 0),
        taxPercent: Number(line['taxPercent'] || 0)
      }))
    };
  }

  private bootstrap(): void {
    this.loading = true;
    forkJoin({ suppliers: this.api.getSuppliers(), products: this.api.getProducts(), warehouses: this.api.getWarehouses() })
      .pipe(finalize(() => { this.loading = false; this.reloadList(); this.cdr.markForCheck(); }))
      .subscribe({
        next: ({ suppliers, products, warehouses }) => {
          this.parties = (suppliers || []).map((s) => ({ id: s.id, label: `${s.code} - ${s.nameEn}` }));
          this.products = products || [];
          this.warehouses = warehouses || [];
        },
        error: () => { this.errorKey = 'COMMON.ERROR_LOADING'; }
      });
  }
}
