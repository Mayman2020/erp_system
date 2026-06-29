import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { forkJoin, Observable } from 'rxjs';
import { finalize } from 'rxjs/operators';
import { PurchaseInvoiceDto, PurchaseInvoiceForm } from '../../core/models/erp.models';
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
export class InvoicesPageComponent extends ErpDocumentPageBase<PurchaseInvoiceDto, PurchaseInvoiceForm> implements OnInit, OnDestroy {
  readonly config: DocumentPageConfig = {
    titleKey: 'MENU.PURCHASE_INVOICES',
    numberKey: 'INVOICES.NUMBER',
    dateKey: 'INVOICES.DATE',
    partyKey: 'MENU.SUPPLIERS',
    partyField: 'supplierId',
    numberField: 'invoiceNumber',
    dateField: 'invoiceDate',
    mode: 'full',
    priceField: 'costPrice',
    showWarehouse: true,
    showDueDate: true,
    dueDateField: 'dueDate'
  };

  readonly columns: DataTableColumn[] = [
    { key: 'invoiceNumber', title: 'ERP.NUMBER' },
    { key: 'invoiceDate', title: 'ERP.DATE', kind: 'date' },
    { key: 'supplierName', title: 'MENU.SUPPLIERS', align: 'start' },
    { key: 'totalAmount', title: 'ERP.TOTAL', align: 'end' },
    { key: 'remainingAmount', title: 'ERP.REMAINING', align: 'end' },
    { key: 'status', title: 'COMMON.STATUS', kind: 'status', prefix: 'STATUS.' }
  ];

  private supplierNameById = new Map<number, string>();

  readonly form = this.fb.group({
    invoiceNumber: [''],
    invoiceDate: ['', Validators.required],
    dueDate: ['', Validators.required],
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

  protected reloadList(): void {
    this.loading = true;
    const params: Record<string, string> = { search: this.filters.query || '' };
    if (this.filters.status) params.status = this.filters.status;
    if (this.filters.fromDate) params.fromDate = this.filters.fromDate;
    if (this.filters.toDate) params.toDate = this.filters.toDate;
    this.api.getPurchaseInvoices(params).pipe(finalize(() => {
      this.loading = false;
      this.cdr.markForCheck();
    })).subscribe({
      next: (rows) => {
        this.rows = (rows || []).map((row) => {
          const mapped = mapAmountRow(row as unknown as Record<string, unknown>);
          mapped['supplierName'] = row.supplierName || this.supplierNameById.get(row.supplierId) || row.supplierId;
          return mapped;
        });
      },
      error: () => { this.errorKey = 'COMMON.ERROR_LOADING'; this.rows = []; }
    });
  }

  protected fetchById(id: number): Observable<PurchaseInvoiceDto> { return this.api.getPurchaseInvoice(id); }
  protected createRequest(payload: PurchaseInvoiceForm): Observable<PurchaseInvoiceDto> { return this.api.createPurchaseInvoice(payload); }
  protected updateRequest(id: number, payload: PurchaseInvoiceForm): Observable<PurchaseInvoiceDto> { return this.api.updatePurchaseInvoice(id, payload); }
  protected deleteRequest(id: number): Observable<void> { return this.api.deletePurchaseInvoice(id); }
  protected approveRequest(id: number, actor: string): Observable<PurchaseInvoiceDto> { return this.api.approvePurchaseInvoice(id, actor); }
  protected cancelRequest(id: number, actor: string): Observable<PurchaseInvoiceDto> { return this.api.cancelPurchaseInvoice(id, actor); }

  protected buildPayload(): PurchaseInvoiceForm {
    const raw = this.form.getRawValue();
    return {
      invoiceNumber: raw.invoiceNumber || undefined,
      invoiceDate: raw.invoiceDate,
      dueDate: raw.dueDate,
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
          this.supplierNameById = new Map(this.parties.map((p) => [p.id, p.label]));
          this.products = products || [];
          this.warehouses = warehouses || [];
        },
        error: () => { this.errorKey = 'COMMON.ERROR_LOADING'; }
      });
  }
}
