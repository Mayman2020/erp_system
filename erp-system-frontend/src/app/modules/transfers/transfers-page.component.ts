import { Component, OnInit } from '@angular/core';
import { TransferDto } from '../../core/models/accounting.models';
import { AccountingApiService } from '../../core/services/accounting-api.service';
@Component({ selector: 'app-transfers-page', templateUrl: './transfers-page.component.html' })
export class TransfersPageComponent implements OnInit {
  titleKey = 'NAV.TRANSFERS';
  loading = false;
  errorKey = '';
  rows: Array<Record<string, unknown>> = [];
  columns = [
    { key: 'reference', title: 'TRANSFERS.REFERENCE', align: 'start' as 'start' },
    { key: 'transferDate', title: 'TRANSFERS.DATE' },
    { key: 'sourceAccountName', title: 'TRANSFERS.SOURCE', align: 'start' as 'start' },
    { key: 'destinationAccountName', title: 'TRANSFERS.DESTINATION', align: 'start' as 'start' },
    { key: 'amount', title: 'TRANSFERS.AMOUNT', align: 'end' as 'end' },
    { key: 'status', title: 'COMMON.STATUS', kind: 'status' as const }
  ];
  private filters: Record<string, string> = {};
  constructor(private api: AccountingApiService) {}
  ngOnInit(): void { this.load(); }
  onSearch(filters: Record<string, string>): void { this.filters = filters || {}; this.load(); }
  private load(): void {
    this.loading = true;
    this.errorKey = '';
    this.api.getTransfers({ search: this.filters.query || '', status: this.filters.status || '', fromDate: this.filters.fromDate || '', toDate: this.filters.toDate || '' })
      .subscribe((rows: TransferDto[]) => {
        this.loading = false;
        this.rows = rows.map((row) => ({ ...row }));
      }, () => {
        this.loading = false;
        this.errorKey = 'COMMON.ERROR_LOADING';
        this.rows = [];
      });
  }
}

