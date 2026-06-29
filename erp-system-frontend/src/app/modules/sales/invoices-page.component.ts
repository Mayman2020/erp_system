import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { forkJoin, Observable } from 'rxjs';
import { finalize } from 'rxjs/operators';
import { SalesInvoiceDto, SalesInvoiceForm } from '../../core/models/erp.models';
import { AuthService } from '../../core/auth/auth.service';
import { ConfirmDialogService } from '../../core/services/confirm-dialog.service';
import { ErpApiService } from '../../core/services/erp-api.service';
import { DataTableColumn } from '../../shared/components/data-table/data-table.component';
import { DocumentPageConfig, ErpDocumentPageBase } from '../../shared/utils/erp-document-page.base';
import { mapAmountRow } from '../../shared/utils/erp-document.utils';

@Component({
  standalone: false,
  selector: 'app-invoices-page',
  templateUrl: './invoices-page.component.html',
  styleUrls: ['./invoices-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class InvoicesPageComponent extends ErpDocumentPageBase<SalesInvoiceDto, SalesInvoiceForm> implements OnInit, OnDestroy {
  readonly config: DocumentPageConfig = {
    titleKey: 'MENU.SALES_INVOICES',
    numberKey: 'INVOICES.NUMBER',
    dateKey: 'INVOICES.DATE',
    partyKey: 'MENU.CUSTOMERS',
    partyField: 'customerId',
    numberField: 'invoiceNumber',
    dateField: 'invoiceDate',
    mode: 'full',
    priceField: 'salePrice',
    showWarehouse: true,
    showDueDate: true,
    dueDateField: 'dueDate'
  };

  readonly columns: DataTableColumn[] = [
    { key: 'invoiceNumber', title: 'ERP.NUMBER' },
    { key: 'invoiceDate', title: 'ERP.DATE', kind: 'date' },
    { key: 'customerName', title: 'MENU.CUSTOMERS', align: 'start' },
    { key: 'totalAmount', title: 'ERP.TOTAL', align: 'end' },
    { key: 'remainingAmount', title: 'ERP.REMAINING', align: 'end' },
    { key: 'status', title: 'COMMON.STATUS', kind: 'status', prefix: 'STATUS.' }
  ];

  readonly form = this.fb.group({
    invoiceNumber: [''],
    invoiceDate: ['', Validators.required],
    dueDate: ['', Validators.required],
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

  protected reloadList(): void {
    this.loading = true;
    const params: Record<string, string> = { search: this.filters.query || '' };
    if (this.filters.status) params.status = this.filters.status;
    if (this.filters.fromDate) params.fromDate = this.filters.fromDate;
    if (this.filters.toDate) params.toDate = this.filters.toDate;
    this.api.getSalesInvoices(params).pipe(finalize(() => {
      this.loading = false;
      this.cdr.markForCheck();
    })).subscribe({
      next: (rows) => { this.rows = (rows || []).map((r) => mapAmountRow(r as unknown as Record<string, unknown>)); },
      error: () => { this.errorKey = 'COMMON.ERROR_LOADING'; this.rows = []; }
    });
  }

  protected fetchById(id: number): Observable<SalesInvoiceDto> { return this.api.getSalesInvoice(id); }
  protected createRequest(payload: SalesInvoiceForm): Observable<SalesInvoiceDto> { return this.api.createSalesInvoice(payload); }
  protected updateRequest(id: number, payload: SalesInvoiceForm): Observable<SalesInvoiceDto> { return this.api.updateSalesInvoice(id, payload); }
  protected deleteRequest(id: number): Observable<void> { return this.api.deleteSalesInvoice(id); }
  protected approveRequest(id: number, actor: string): Observable<SalesInvoiceDto> { return this.api.approveSalesInvoice(id, actor); }
  protected cancelRequest(id: number, actor: string): Observable<SalesInvoiceDto> { return this.api.cancelSalesInvoice(id, actor); }

  protected buildPayload(): SalesInvoiceForm {
    const raw = this.form.getRawValue();
    return {
      invoiceNumber: raw.invoiceNumber || undefined,
      invoiceDate: raw.invoiceDate,
      dueDate: raw.dueDate,
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
