import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { forkJoin, Observable } from 'rxjs';
import { finalize } from 'rxjs/operators';
import { PurchaseReturnDto, PurchaseReturnForm } from '../../core/models/erp.models';
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
export class ReturnsPageComponent extends ErpDocumentPageBase<PurchaseReturnDto, PurchaseReturnForm> implements OnInit, OnDestroy {
  readonly config: DocumentPageConfig = {
    titleKey: 'MENU.PURCHASE_RETURNS',
    numberKey: 'ERP.NUMBER',
    dateKey: 'ERP.DATE',
    partyKey: 'MENU.SUPPLIERS',
    partyField: 'supplierId',
    numberField: 'returnNumber',
    dateField: 'returnDate',
    mode: 'return',
    priceField: 'costPrice',
    showWarehouse: true,
    showHeaderTax: true,
    showInvoiceLink: true
  };

  readonly columns: DataTableColumn[] = [
    { key: 'returnNumber', title: 'ERP.NUMBER' },
    { key: 'returnDate', title: 'ERP.DATE', kind: 'date' },
    { key: 'supplierName', title: 'MENU.SUPPLIERS', align: 'start' },
    { key: 'totalAmount', title: 'ERP.TOTAL', align: 'end' },
    { key: 'status', title: 'COMMON.STATUS', kind: 'status', prefix: 'STATUS.' }
  ];

  readonly form = this.fb.group({
    returnNumber: [''],
    returnDate: ['', Validators.required],
    supplierId: [null as number | null, Validators.required],
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
    this.api.getPurchaseReturns().pipe(finalize(() => {
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

  protected fetchById(id: number): Observable<PurchaseReturnDto> { return this.api.getPurchaseReturn(id); }
  protected createRequest(payload: PurchaseReturnForm): Observable<PurchaseReturnDto> { return this.api.createPurchaseReturn(payload); }
  protected updateRequest(id: number, payload: PurchaseReturnForm): Observable<PurchaseReturnDto> { return this.api.updatePurchaseReturn(id, payload); }
  protected deleteRequest(id: number): Observable<void> { return this.api.deletePurchaseReturn(id); }
  protected approveRequest(id: number, actor: string): Observable<PurchaseReturnDto> { return this.api.approvePurchaseReturn(id, actor); }
  protected cancelRequest(id: number, actor: string): Observable<PurchaseReturnDto> { return this.api.cancelPurchaseReturn(id, actor); }

  protected buildPayload(): PurchaseReturnForm {
    const raw = this.form.getRawValue();
    return {
      returnNumber: raw.returnNumber || undefined,
      returnDate: raw.returnDate,
      supplierId: Number(raw.supplierId),
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
