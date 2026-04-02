import { Component, OnInit } from '@angular/core';
import { AccountingCheckDto } from '../../core/models/accounting.models';
import { AccountingApiService } from '../../core/services/accounting-api.service';
@Component({ selector: 'app-checks-page', templateUrl: './checks-page.component.html' })
export class ChecksPageComponent implements OnInit {
  titleKey = 'NAV.CHECKS';
  loading = false;
  errorKey = '';
  rows: Array<Record<string, unknown>> = [];
  columns = [
    { key: 'checkNumber', title: 'CHECKS.NUMBER', align: 'start' as 'start' },
    { key: 'checkType', title: 'CHECKS.TYPE', kind: 'type' as 'type', prefix: 'CHECK_TYPE.' },
    { key: 'bankName', title: 'CHECKS.BANK', align: 'start' as 'start' },
    { key: 'dueDate', title: 'CHECKS.DUE_DATE' },
    { key: 'partyName', title: 'CHECKS.PARTY', align: 'start' as 'start' },
    { key: 'amount', title: 'CHECKS.AMOUNT', align: 'end' as 'end' },
    { key: 'status', title: 'COMMON.STATUS', kind: 'status' as const }
  ];
  private filters: Record<string, string> = {};
  constructor(private api: AccountingApiService) {}
  ngOnInit(): void { this.load(); }
  onSearch(filters: Record<string, string>): void { this.filters = filters || {}; this.load(); }
  private load(): void {
    this.loading = true;
    this.errorKey = '';
    this.api.getChecks({ search: this.filters.query || '' })
      .subscribe((rows: AccountingCheckDto[]) => {
        this.loading = false;
        this.rows = rows.map((row) => ({ ...row }));
      }, () => {
        this.loading = false;
        this.errorKey = 'COMMON.ERROR_LOADING';
        this.rows = [];
      });
  }
}

