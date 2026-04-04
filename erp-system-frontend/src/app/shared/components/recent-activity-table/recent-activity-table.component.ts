import { ChangeDetectorRef, Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { PagedResult } from '../../../core/models/api.models';
import { RecentActivityItem, RecentActivityKind } from '../../../core/models/accounting.models';
import { AccountingApiService } from '../../../core/services/accounting-api.service';
import { DataTableColumn } from '../data-table/data-table.component';
import { finalize } from 'rxjs/operators';

type SortDirection = 'asc' | 'desc';

@Component({
  standalone: false,
  selector: 'app-recent-activity-table',
  templateUrl: './recent-activity-table.component.html'
})
export class RecentActivityTableComponent implements OnInit, OnChanges {
  @Input() titleKey = '';
  @Input() kind: RecentActivityKind = 'journals';
  @Input() viewAllLink: any[] | string = '/dashboard';
  @Input() pageSize = 5;

  readonly columns: DataTableColumn[] = [
    { key: 'reference', title: 'DASHBOARD.DOC_REFERENCE', align: 'start', className: 'erp-table__col--compact' },
    { key: 'date', title: 'DASHBOARD.DOC_DATE', kind: 'date', className: 'erp-table__col--compact' },
    { key: 'amountDisplay', title: 'DASHBOARD.DOC_AMOUNT', align: 'end', className: 'erp-table__col--compact' },
    { key: 'status', title: 'COMMON.STATUS', kind: 'status' }
  ];

  loading = false;
  rows: Array<Record<string, unknown>> = [];
  page = 1;
  totalItems = 0;
  totalPages = 1;
  sortDirection: SortDirection = 'desc';

  constructor(
    private accountingApiService: AccountingApiService,
    private readonly cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.load();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if ((changes['kind'] && !changes['kind'].firstChange) || (changes['pageSize'] && !changes['pageSize'].firstChange)) {
      this.page = 1;
      this.load();
    }
  }

  toggleSortDirection(): void {
    this.sortDirection = this.sortDirection === 'desc' ? 'asc' : 'desc';
    this.page = 1;
    this.load();
  }

  onPageChange(page: number): void {
    if (page === this.page) {
      return;
    }
    this.page = page;
    this.load();
  }

  private load(): void {
    this.loading = true;
    this.cdr.detectChanges();
    this.accountingApiService
      .getRecentActivity(this.kind, this.page - 1, this.pageSize, 'date', this.sortDirection)
      .pipe(
        finalize(() => {
          this.loading = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: (result: PagedResult<RecentActivityItem>) => {
          this.rows = (result.items || []).map((item) => ({
            id: item.id,
            reference: item.reference,
            date: item.date,
            amountDisplay: this.formatAmount(item.amount),
            status: item.status
          }));
          this.totalItems = result.totalItems || 0;
          this.totalPages = result.totalPages || 1;
          this.page = (result.page || 0) + 1;
        },
        error: () => {
          this.rows = [];
          this.totalItems = 0;
          this.totalPages = 1;
        }
      });
  }

  private formatAmount(value: number): string {
    return Number(value || 0).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  }
}
