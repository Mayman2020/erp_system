import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { forkJoin, Observable } from 'rxjs';
import { finalize } from 'rxjs/operators';
import { SalesOrderDto, SalesOrderForm } from '../../core/models/erp.models';
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
export class OrdersPageComponent extends ErpDocumentPageBase<SalesOrderDto, SalesOrderForm> implements OnInit, OnDestroy {
  readonly config: DocumentPageConfig = {
    titleKey: 'MENU.SALES_ORDERS',
    numberKey: 'ERP.NUMBER',
    dateKey: 'ERP.DATE',
    partyKey: 'MENU.CUSTOMERS',
    partyField: 'customerId',
    numberField: 'orderNumber',
    dateField: 'orderDate',
    mode: 'full',
    priceField: 'salePrice',
    showWarehouse: true
  };

  readonly columns: DataTableColumn[] = [
    { key: 'orderNumber', title: 'ERP.NUMBER' },
    { key: 'orderDate', title: 'ERP.DATE', kind: 'date' },
    { key: 'customerName', title: 'MENU.CUSTOMERS', align: 'start' },
    { key: 'totalAmount', title: 'ERP.TOTAL', align: 'end' },
    { key: 'status', title: 'COMMON.STATUS', kind: 'status', prefix: 'STATUS.' }
  ];

  readonly form = this.fb.group({
    orderNumber: [''],
    orderDate: ['', Validators.required],
    customerId: [null as number | null, Validators.required],
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
        this.api.convertSalesOrderToInvoice(id, this.actorEmail).subscribe({
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
    const params: Record<string, string> = { search: this.filters.query || '' };
    if (this.filters.status) params.status = this.filters.status;
    if (this.filters.fromDate) params.fromDate = this.filters.fromDate;
    if (this.filters.toDate) params.toDate = this.filters.toDate;
    this.api.getSalesOrders(params).pipe(finalize(() => {
      this.loading = false;
      this.cdr.markForCheck();
    })).subscribe({
      next: (rows) => { this.rows = (rows || []).map((r) => mapAmountRow(r as unknown as Record<string, unknown>)); },
      error: () => { this.errorKey = 'COMMON.ERROR_LOADING'; this.rows = []; }
    });
  }

  protected fetchById(id: number): Observable<SalesOrderDto> { return this.api.getSalesOrder(id); }
  protected createRequest(payload: SalesOrderForm): Observable<SalesOrderDto> { return this.api.createSalesOrder(payload); }
  protected updateRequest(id: number, payload: SalesOrderForm): Observable<SalesOrderDto> { return this.api.updateSalesOrder(id, payload); }
  protected deleteRequest(id: number): Observable<void> { return this.api.deleteSalesOrder(id); }
  protected approveRequest(id: number, actor: string): Observable<SalesOrderDto> { return this.api.approveSalesOrder(id, actor); }
  protected cancelRequest(id: number, actor: string): Observable<SalesOrderDto> { return this.api.cancelSalesOrder(id, actor); }

  protected buildPayload(): SalesOrderForm {
    const raw = this.form.getRawValue();
    return {
      orderNumber: raw.orderNumber || undefined,
      orderDate: raw.orderDate,
      customerId: Number(raw.customerId),
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
    forkJoin({ customers: this.api.getCustomers(), products: this.api.getProducts(), warehouses: this.api.getWarehouses() })
      .pipe(finalize(() => { this.loading = false; this.reloadList(); this.cdr.markForCheck(); }))
      .subscribe({
        next: ({ customers, products, warehouses }) => {
          this.parties = (customers || []).map((c) => ({ id: c.id, label: `${c.code} - ${c.nameEn}` }));
          this.products = products || [];
          this.warehouses = warehouses || [];
        },
        error: () => { this.errorKey = 'COMMON.ERROR_LOADING'; }
      });
  }
}
