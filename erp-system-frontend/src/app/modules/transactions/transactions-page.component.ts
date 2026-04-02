import { Component, OnInit } from '@angular/core';
import { AccountingTransactionDto } from '../../core/models/accounting.models';
import { AccountingApiService } from '../../core/services/accounting-api.service';
@Component({ selector: 'app-transactions-page', templateUrl: './transactions-page.component.html' })
export class TransactionsPageComponent implements OnInit {
  titleKey = 'NAV.TRANSACTIONS';
  loading = false;
  errorKey = '';
  rows: Array<Record<string, unknown>> = [];
  columns = [
    { key: 'reference', title: 'TRANSACTIONS.REFERENCE', align: 'start' as 'start' },
    { key: 'transactionDate', title: 'TRANSACTIONS.DATE' },
    { key: 'transactionType', title: 'TRANSACTIONS.TYPE', kind: 'type' as 'type', prefix: 'TRANSACTION_TYPE.' },
    { key: 'debitAccountName', title: 'TRANSACTIONS.DEBIT_ACCOUNT', align: 'start' as 'start' },
    { key: 'creditAccountName', title: 'TRANSACTIONS.CREDIT_ACCOUNT', align: 'start' as 'start' },
    { key: 'amount', title: 'TRANSACTIONS.AMOUNT', align: 'end' as 'end' },
    { key: 'status', title: 'COMMON.STATUS', kind: 'status' as const }
  ];
  private filters: Record<string, string> = {};
  constructor(private api: AccountingApiService) {}
  ngOnInit(): void { this.load(); }
  onSearch(filters: Record<string, string>): void { this.filters = filters || {}; this.load(); }
  private load(): void {
    this.loading = true;
    this.errorKey = '';
    this.api.getTransactions({ search: this.filters.query || '', status: this.filters.status || '' })
      .subscribe((rows: AccountingTransactionDto[]) => {
        this.loading = false;
        this.rows = rows.map((row) => ({ ...row }));
      }, () => {
        this.loading = false;
        this.errorKey = 'COMMON.ERROR_LOADING';
        this.rows = [];
      });
  }
}

