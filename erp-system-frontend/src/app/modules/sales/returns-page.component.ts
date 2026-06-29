import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { forkJoin, Observable } from 'rxjs';
import { finalize } from 'rxjs/operators';
import { SalesReturnDto, SalesReturnForm } from '../../core/models/erp.models';
import { AuthService } from '../../core/auth/auth.service';
import { ConfirmDialogService } from '../../core/services/confirm-dialog.service';
import { ErpApiService } from '../../core/services/erp-api.service';
import { DataTableColumn } from '../../shared/components/data-table/data-table.component';
import { DocumentPageConfig, ErpDocumentPageBase } from '../../shared/utils/erp-document-page.base';
import { mapAmountRow } from '../../shared/utils/erp-document.utils';

@Component({
  standalone: false,
  selector: 'app-returns-page',
  templateUrl: './returns-page.component.html',
  styleUrls: ['./returns-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ReturnsPageComponent extends ErpDocumentPageBase<SalesReturnDto, SalesReturnForm> implements OnInit, OnDestroy {
  readonly config: DocumentPageConfig = {
    titleKey: 'MENU.SALES_RETURNS',
    numberKey: 'ERP.NUMBER',
    dateKey: 'ERP.DATE',
    partyKey: 'MENU.CUSTOMERS',
    partyField: 'customerId',
    numberField: 'returnNumber',
    dateField: 'returnDate',
    mode: 'return',
    priceField: 'salePrice',
    showWarehouse: true,
    showHeaderTax: true,
    showInvoiceLink: true
  };

  readonly columns: DataTableColumn[] = [
    { key: 'returnNumber', title: 'ERP.NUMBER' },
    { key: 'returnDate', title: 'ERP.DATE', kind: 'date' },
    { key: 'customerName', title: 'MENU.CUSTOMERS', align: 'start' },
    { key: 'totalAmount', title: 'ERP.TOTAL', align: 'end' },
    { key: 'status', title: 'COMMON.STATUS', kind: 'status', prefix: 'STATUS.' }
  ];

  readonly form = this.fb.group({
    returnNumber: [''],
    returnDate: ['', Validators.required],
    customerId: [null as number | null, Validators.required],
    invoiceId: [null as number | null],
    warehouseId: [null as number | null],
    taxAmount: [0],
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

  protected reloadList(): void {
    this.loading = true;
    const params: Record<string, string> = { search: this.filters.query || '' };
    if (this.filters.status) params.status = this.filters.status;
    if (this.filters.fromDate) params.fromDate = this.filters.fromDate;
    if (this.filters.toDate) params.toDate = this.filters.toDate;
    this.api.getSalesReturns(params).pipe(finalize(() => {
      this.loading = false;
      this.cdr.markForCheck();
    })).subscribe({
      next: (rows) => { this.rows = (rows || []).map((r) => mapAmountRow(r as unknown as Record<string, unknown>)); },
      error: () => { this.errorKey = 'COMMON.ERROR_LOADING'; this.rows = []; }
    });
  }

  protected fetchById(id: number): Observable<SalesReturnDto> { return this.api.getSalesReturn(id); }
  protected createRequest(payload: SalesReturnForm): Observable<SalesReturnDto> { return this.api.createSalesReturn(payload); }
  protected updateRequest(id: number, payload: SalesReturnForm): Observable<SalesReturnDto> { return this.api.updateSalesReturn(id, payload); }
  protected deleteRequest(id: number): Observable<void> { return this.api.deleteSalesReturn(id); }
  protected approveRequest(id: number, actor: string): Observable<SalesReturnDto> { return this.api.approveSalesReturn(id, actor); }
  protected cancelRequest(id: number, actor: string): Observable<SalesReturnDto> { return this.api.cancelSalesReturn(id, actor); }

  protected buildPayload(): SalesReturnForm {
    const raw = this.form.getRawValue();
    return {
      returnNumber: raw.returnNumber || undefined,
      returnDate: raw.returnDate,
      customerId: Number(raw.customerId),
      invoiceId: raw.invoiceId ? Number(raw.invoiceId) : undefined,
      warehouseId: raw.warehouseId ? Number(raw.warehouseId) : undefined,
      taxAmount: Number(raw.taxAmount || 0),
      notes: raw.notes || undefined,
      lines: (raw.lines || []).map((line: Record<string, unknown>) => ({
        productId: Number(line['productId']),
        quantity: Number(line['quantity']),
        unitPrice: Number(line['unitPrice'])
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
