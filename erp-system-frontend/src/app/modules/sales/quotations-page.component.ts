import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { forkJoin, Observable } from 'rxjs';
import { finalize } from 'rxjs/operators';
import { SalesQuotationDto, SalesQuotationForm } from '../../core/models/erp.models';
import { AuthService } from '../../core/auth/auth.service';
import { ConfirmDialogService } from '../../core/services/confirm-dialog.service';
import { ErpApiService } from '../../core/services/erp-api.service';
import { DataTableAction, DataTableColumn } from '../../shared/components/data-table/data-table.component';
import { DocumentPageConfig, ErpDocumentPageBase } from '../../shared/utils/erp-document-page.base';
import { DOCUMENT_ACTIONS, mapAmountRow } from '../../shared/utils/erp-document.utils';

@Component({
  standalone: false,
  selector: 'app-quotations-page',
  templateUrl: './quotations-page.component.html',
  styleUrls: ['./quotations-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class QuotationsPageComponent extends ErpDocumentPageBase<SalesQuotationDto, SalesQuotationForm> implements OnInit, OnDestroy {
  readonly config: DocumentPageConfig = {
    titleKey: 'MENU.SALES_QUOTATIONS',
    numberKey: 'ERP.NUMBER',
    dateKey: 'ERP.DATE',
    partyKey: 'MENU.CUSTOMERS',
    partyField: 'customerId',
    numberField: 'quotationNumber',
    dateField: 'quotationDate',
    mode: 'full',
    priceField: 'salePrice',
    showValidUntil: true
  };

  readonly columns: DataTableColumn[] = [
    { key: 'quotationNumber', title: 'ERP.NUMBER' },
    { key: 'quotationDate', title: 'ERP.DATE', kind: 'date' },
    { key: 'customerName', title: 'MENU.CUSTOMERS', align: 'start' },
    { key: 'totalAmount', title: 'ERP.TOTAL', align: 'end' },
    { key: 'status', title: 'COMMON.STATUS', kind: 'status', prefix: 'STATUS.' }
  ];

  readonly form = this.fb.group({
    quotationNumber: [''],
    quotationDate: ['', Validators.required],
    validUntil: [''],
    customerId: [null as number | null, Validators.required],
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
    { id: 'convert', labelKey: 'ERP.CONVERT_TO_ORDER', className: 'erp-action-success', disabledWhen: (r) => String(r['status']) !== 'APPROVED' },
    ...DOCUMENT_ACTIONS.slice(3)
  ];

  override onTableAction(event: { actionId: string; row: Record<string, unknown> }): void {
    const id = Number(event.row['id']);
    if (event.actionId === 'convert' && id) {
      this.confirmDialog.confirmByKey({ messageKey: 'ERP.CONVERT_TO_ORDER_CONFIRM' }).subscribe((ok) => {
        if (!ok) return;
        this.api.convertQuotationToOrder(id, this.actorEmail).subscribe({
          next: () => {
            this.showSuccess('ERP.CONVERT_TO_ORDER_SUCCESS');
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
    this.api.getSalesQuotations(params).pipe(finalize(() => {
      this.loading = false;
      this.cdr.markForCheck();
    })).subscribe({
      next: (rows) => {
        this.rows = (rows || []).map((r) => mapAmountRow(r as unknown as Record<string, unknown>));
      },
      error: () => {
        this.errorKey = 'COMMON.ERROR_LOADING';
        this.rows = [];
      }
    });
  }

  protected fetchById(id: number): Observable<SalesQuotationDto> {
    return this.api.getSalesQuotation(id);
  }

  protected createRequest(payload: SalesQuotationForm): Observable<SalesQuotationDto> {
    return this.api.createSalesQuotation(payload);
  }

  protected updateRequest(id: number, payload: SalesQuotationForm): Observable<SalesQuotationDto> {
    return this.api.updateSalesQuotation(id, payload);
  }

  protected deleteRequest(id: number): Observable<void> {
    return this.api.deleteSalesQuotation(id);
  }

  protected approveRequest(id: number, actor: string): Observable<SalesQuotationDto> {
    return this.api.approveSalesQuotation(id, actor);
  }

  protected cancelRequest(id: number, actor: string): Observable<SalesQuotationDto> {
    return this.api.cancelSalesQuotation(id, actor);
  }

  protected buildPayload(): SalesQuotationForm {
    const raw = this.form.getRawValue();
    return {
      quotationNumber: raw.quotationNumber || undefined,
      quotationDate: raw.quotationDate,
      validUntil: raw.validUntil || undefined,
      customerId: Number(raw.customerId),
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
    forkJoin({ customers: this.api.getCustomers(), products: this.api.getProducts() })
      .pipe(finalize(() => {
        this.loading = false;
        this.reloadList();
        this.cdr.markForCheck();
      }))
      .subscribe({
        next: ({ customers, products }) => {
          this.parties = (customers || []).map((c) => ({ id: c.id, label: `${c.code} - ${c.nameEn}` }));
          this.products = products || [];
        },
        error: () => {
          this.errorKey = 'COMMON.ERROR_LOADING';
        }
      });
  }
}
