import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { finalize } from 'rxjs/operators';
import { AccountingCheckDto } from '../../core/models/accounting.models';
import { AccountingApiService } from '../../core/services/accounting-api.service';
import { LookupService } from '../../core/services/lookup.service';

@Component({ standalone: false, selector: 'app-checks-page', templateUrl: './checks-page.component.html' })
export class ChecksPageComponent implements OnInit {
  titleKey = 'NAV.CHECKS';
  loading = false;
  errorKey = '';
  rows: Array<Record<string, unknown>> = [];
  statusOptions: string[] = [];
  columns = [
    { key: 'checkNumber', title: 'CHECKS.NUMBER', align: 'start' as 'start' },
    { key: 'dueDate', title: 'CHECKS.DUE_DATE', kind: 'date' as 'date' },
    { key: 'amount', title: 'CHECKS.AMOUNT', align: 'end' as 'end' },
    { key: 'status', title: 'COMMON.STATUS', kind: 'status' as const },
    { key: 'checkType', title: 'CHECKS.TYPE', kind: 'type' as 'type', prefix: 'CHECK_TYPE.' },
    { key: 'bankName', title: 'CHECKS.BANK', align: 'start' as 'start' },
    { key: 'partyName', title: 'CHECKS.PARTY', align: 'start' as 'start' }
  ];
  private filters: Record<string, string> = {};

  constructor(private api: AccountingApiService, private lookupService: LookupService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.lookupService.getLookup('check-statuses').subscribe({
      next: (items) => { this.statusOptions = items.map((i) => i.code); },
      error: () => { this.statusOptions = ['PENDING', 'DEPOSITED', 'CLEARED', 'BOUNCED', 'CANCELLED']; }
    });
    this.load();
  }

  onSearch(filters: Record<string, string>): void { this.filters = filters || {}; this.load(); }

  private load(): void {
    this.loading = true;
    this.errorKey = '';
    this.api.getChecks({
      search: this.filters.query || '',
      status: this.filters.status || '',
      fromDate: this.filters.fromDate || '',
      toDate: this.filters.toDate || ''
    })
      .pipe(finalize(() => { this.loading = false; this.cdr.detectChanges(); }))
      .subscribe({
        next: (rows: AccountingCheckDto[]) => {
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

