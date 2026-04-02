import { Component, OnInit } from '@angular/core';
import { BankAccountDto } from '../../core/models/accounting.models';
import { AccountingApiService } from '../../core/services/accounting-api.service';

@Component({ standalone: false,
  selector: 'app-banks-page',
  templateUrl: './banks-page.component.html'
})
export class BanksPageComponent implements OnInit {
  titleKey = 'BANKS.TITLE';
  loading = false;
  errorKey = '';
  rows: Array<Record<string, unknown>> = [];
  columns = [
    { key: 'bankName', title: 'BANKS.BANK_NAME', align: 'start' as 'start' },
    { key: 'accountNumber', title: 'BANKS.ACCOUNT_NUMBER', align: 'start' as 'start' },
    { key: 'currency', title: 'BANKS.CURRENCY' },
    { key: 'openingBalance', title: 'BANKS.OPENING_BALANCE', align: 'end' as 'end' },
    { key: 'currentBalance', title: 'BANKS.CURRENT_BALANCE', align: 'end' as 'end' },
    { key: 'linkedAccountName', title: 'BANKS.LINKED_ACCOUNT', align: 'start' as 'start' },
    { key: 'active', title: 'COMMON.STATUS', kind: 'boolean' as const }
  ];
  private filters: Record<string, string> = {};

  constructor(private api: AccountingApiService) {}

  ngOnInit(): void {
    this.load();
  }

  onSearch(filters: Record<string, string>): void {
    this.filters = filters || {};
    this.load();
  }

  private load(): void {
    this.loading = true;
    this.errorKey = '';
    this.api
      .getBankAccounts({ search: this.filters.query || '' })
      .subscribe((rows: BankAccountDto[]) => {
        this.loading = false;
        this.rows = rows.map((row) => ({ ...row }));
      }, () => {
        this.loading = false;
        this.errorKey = 'COMMON.ERROR_LOADING';
        this.rows = [];
      });
  }
}
