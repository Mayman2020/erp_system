import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { finalize } from 'rxjs/operators';
import { CustomerInvoiceDto } from '../../core/models/accounting.models';
import { AccountingApiService } from '../../core/services/accounting-api.service';
import { LookupService } from '../../core/services/lookup.service';

@Component({ standalone: false, selector: 'app-invoices-page', templateUrl: './invoices-page.component.html' })
export class InvoicesPageComponent implements OnInit {
  titleKey = 'NAV.INVOICES';
  loading = false;
  errorKey = '';
  rows: Array<Record<string, unknown>> = [];
  statusOptions: string[] = [];
  columns = [
    { key: 'invoiceNumber', title: 'INVOICES.NUMBER', align: 'start' as 'start' },
    { key: 'invoiceDate', title: 'INVOICES.DATE', kind: 'date' as 'date' },
    { key: 'customerName', title: 'INVOICES.CUSTOMER', align: 'start' as 'start' },
    { key: 'totalAmount', title: 'INVOICES.TOTAL', align: 'end' as 'end' },
    { key: 'status', title: 'COMMON.STATUS', kind: 'status' as const },
    { key: 'outstandingAmount', title: 'INVOICES.OUTSTANDING', align: 'end' as 'end' }
  ];
  private filters: Record<string, string> = {};

  constructor(private api: AccountingApiService, private lookupService: LookupService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.lookupService.getLookup('invoice-statuses').subscribe({
      next: (items) => { this.statusOptions = items.map((i) => i.code); },
      error: () => { this.statusOptions = ['DRAFT', 'APPROVED', 'PAID', 'CANCELLED']; }
    });
    this.load();
  }

  onSearch(filters: Record<string, string>): void { this.filters = filters || {}; this.load(); }

  private load(): void {
    this.loading = true;
    this.errorKey = '';
    this.api.getInvoices({
      search: this.filters.query || '',
      status: this.filters.status || '',
      fromDate: this.filters.fromDate || '',
      toDate: this.filters.toDate || ''
    })
      .pipe(finalize(() => { this.loading = false; this.cdr.detectChanges(); }))
      .subscribe({
        next: (rows: CustomerInvoiceDto[]) => {
          this.rows = rows.map((row) => ({
            ...row,
            totalAmount: Number(row.totalAmount || 0).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 }),
            outstandingAmount: Number(row.outstandingAmount || 0).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })
          }));
        },
        error: () => {
          this.errorKey = 'COMMON.ERROR_LOADING';
          this.rows = [];
        }
      });
  }
}

