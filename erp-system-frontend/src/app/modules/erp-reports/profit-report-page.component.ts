import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { finalize } from 'rxjs/operators';
import { ErpApiService } from '../../core/services/erp-api.service';

@Component({
  standalone: false,
  selector: 'app-profit-report-page',
  templateUrl: './profit-report-page.component.html',
  styleUrls: ['./profit-report-page.component.scss']
})
export class ProfitReportPageComponent implements OnInit {
  titleKey = 'MENU.PROFIT_REPORT';
  loading = false;
  errorKey = '';
  summary: Record<string, unknown> = {};
  rows: Array<Record<string, unknown>> = [];
  columns = [];
  dateFilter = true;

  private filters: Record<string, string> = {};

  constructor(private api: ErpApiService, private cdr: ChangeDetectorRef) {}

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
    const fromDate = this.filters.fromDate || '';
    const toDate = this.filters.toDate || '';

    this.api.getProfitReport(fromDate || undefined, toDate || undefined)
      .pipe(finalize(() => { this.loading = false; this.cdr.detectChanges(); }))
      .subscribe({
        next: (data) => {
          this.summary = {
            fromDate: data?.fromDate,
            toDate: data?.toDate,
            totalSales: this.formatAmount(data?.totalSales),
            totalPurchases: this.formatAmount(data?.totalPurchases),
            netProfit: this.formatAmount(data?.netProfit)
          };
          this.rows = [];
        },
        error: () => {
          this.errorKey = 'COMMON.ERROR_LOADING';
          this.summary = {};
          this.rows = [];
        }
      });
  }

  private formatAmount(value: unknown): string {
    return Number(value || 0).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  }
}
