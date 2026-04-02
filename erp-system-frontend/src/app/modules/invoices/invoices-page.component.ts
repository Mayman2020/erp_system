import { Component, OnInit } from '@angular/core';
import { CustomerInvoiceDto } from '../../core/models/accounting.models';
import { AccountingApiService } from '../../core/services/accounting-api.service';
@Component({ standalone: false, selector: 'app-invoices-page', templateUrl: './invoices-page.component.html' })
export class InvoicesPageComponent implements OnInit {
  titleKey = 'NAV.INVOICES';
  loading = false;
  errorKey = '';
  rows: Array<Record<string, unknown>> = [];
  columns = [
    { key: 'invoiceNumber', title: 'INVOICES.NUMBER', align: 'start' as 'start' },
    { key: 'invoiceDate', title: 'INVOICES.DATE' },
    { key: 'customerName', title: 'INVOICES.CUSTOMER', align: 'start' as 'start' },
    { key: 'totalAmount', title: 'INVOICES.TOTAL', align: 'end' as 'end' },
    { key: 'outstandingAmount', title: 'INVOICES.OUTSTANDING', align: 'end' as 'end' },
    { key: 'status', title: 'COMMON.STATUS', kind: 'status' as const }
  ];
  private filters: Record<string, string> = {};
  constructor(private api: AccountingApiService) {}
  ngOnInit(): void { this.load(); }
  onSearch(filters: Record<string, string>): void { this.filters = filters || {}; this.load(); }
  private load(): void {
    this.loading = true;
    this.errorKey = '';
    this.api.getInvoices({ search: this.filters.query || '', status: this.filters.status || '' })
      .subscribe((rows: CustomerInvoiceDto[]) => {
        this.loading = false;
        this.rows = rows.map((row) => ({ ...row }));
      }, () => {
        this.loading = false;
        this.errorKey = 'COMMON.ERROR_LOADING';
        this.rows = [];
      });
  }
}

