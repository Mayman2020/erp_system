import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { finalize } from 'rxjs/operators';
import { AccountingTransactionDto } from '../../core/models/accounting.models';
import { LookupItem } from '../../core/models/lookup.models';
import { AccountingApiService } from '../../core/services/accounting-api.service';
import { LookupService } from '../../core/services/lookup.service';

@Component({ standalone: false, selector: 'app-transactions-page', templateUrl: './transactions-page.component.html' })
export class TransactionsPageComponent implements OnInit {
  titleKey = 'NAV.TRANSACTIONS';
  loading = false;
  errorKey = '';
  rows: Array<Record<string, unknown>> = [];
  statusOptions: string[] = [];
  columns = [
    { key: 'reference', title: 'TRANSACTIONS.REFERENCE', align: 'start' as 'start' },
    { key: 'transactionDate', title: 'TRANSACTIONS.DATE', kind: 'date' as 'date' },
    { key: 'transactionType', title: 'TRANSACTIONS.TYPE', kind: 'type' as 'type', prefix: 'TRANSACTION_TYPE.' },
    { key: 'amount', title: 'TRANSACTIONS.AMOUNT', align: 'end' as 'end' },
    { key: 'status', title: 'COMMON.STATUS', kind: 'status' as const },
    { key: 'debitAccountName', title: 'TRANSACTIONS.DEBIT_ACCOUNT', align: 'start' as 'start' },
    { key: 'creditAccountName', title: 'TRANSACTIONS.CREDIT_ACCOUNT', align: 'start' as 'start' }
  ];
  private filters: Record<string, string> = {};

  constructor(private api: AccountingApiService, private lookupService: LookupService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.lookupService.getLookup('transaction-statuses').subscribe({
      next: (rows: LookupItem[]) => { this.statusOptions = rows.map((item) => item.code); },
      error: () => undefined
    });
    this.load();
  }

  onSearch(filters: Record<string, string>): void { this.filters = filters || {}; this.load(); }

  private load(): void {
    this.loading = true;
    this.errorKey = '';
    this.api.getTransactions({
      search: this.filters.query || '',
      status: this.filters.status || '',
      fromDate: this.filters.fromDate || '',
      toDate: this.filters.toDate || ''
    })
      .pipe(finalize(() => { this.loading = false; this.cdr.detectChanges(); }))
      .subscribe({
        next: (rows: AccountingTransactionDto[]) => {
          this.rows = rows.map((row) => ({
            ...row,
            amount: Number(row.amount || 0).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })
          }));
        },
        error: () => {
          this.errorKey = 'COMMON.ERROR_LOADING';
          this.rows = [];
        }
      });
  }
}

