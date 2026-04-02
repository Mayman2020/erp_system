import { Component, OnInit } from '@angular/core';
import { AccountDto, LedgerDto } from '../../core/models/accounting.models';
import { AccountingApiService } from '../../core/services/accounting-api.service';
@Component({ selector: 'app-ledger-page', templateUrl: './ledger-page.component.html' })
export class LedgerPageComponent implements OnInit {
  titleKey = 'NAV.LEDGER';
  loading = false;
  errorKey = '';
  rows: Array<Record<string, unknown>> = [];
  accounts: AccountDto[] = [];
  selectedAccountId: number | null = null;
  columns = [
    { key: 'journalReference', title: 'LEDGER.REFERENCE', align: 'start' as 'start' },
    { key: 'entryDate', title: 'LEDGER.DATE' },
    { key: 'description', title: 'LEDGER.DESCRIPTION', align: 'start' as 'start' },
    { key: 'debit', title: 'LEDGER.DEBIT', align: 'end' as 'end' },
    { key: 'credit', title: 'LEDGER.CREDIT', align: 'end' as 'end' },
    { key: 'runningBalance', title: 'LEDGER.RUNNING_BALANCE', align: 'end' as 'end' }
  ];
  constructor(private api: AccountingApiService) {}
  ngOnInit(): void {
    this.api.getAccounts({ active: true }).subscribe((accounts) => {
      this.accounts = accounts.filter((account) => account.postable);
      if (this.accounts.length) {
        this.selectedAccountId = this.accounts[0].id;
        this.load();
      }
    }, () => undefined);
  }
  load(): void {
    if (!this.selectedAccountId) {
      this.rows = [];
      return;
    }
    this.loading = true;
    this.errorKey = '';
    this.api.getLedger({ accountId: this.selectedAccountId }).subscribe((ledger: LedgerDto) => {
      this.loading = false;
      this.rows = (ledger.lines || []).map((line) => ({ ...line }));
    }, () => {
      this.loading = false;
      this.errorKey = 'COMMON.ERROR_LOADING';
      this.rows = [];
    });
  }
}

