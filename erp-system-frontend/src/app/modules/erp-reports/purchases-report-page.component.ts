import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { finalize } from 'rxjs/operators';
import { ErpApiService } from '../../core/services/erp-api.service';

@Component({
  standalone: false,
  selector: 'app-purchases-report-page',
  templateUrl: './purchases-report-page.component.html',
  styleUrls: ['./purchases-report-page.component.scss']
})
export class PurchasesReportPageComponent implements OnInit {
  titleKey = 'MENU.PURCHASE_REPORT';
  loading = false;
  errorKey = '';
  summary: Record<string, unknown> = {};
  rows: Array<Record<string, unknown>> = [];
  columns = [
    {
        key: "number",
        title: "ERP.NUMBER"
    },
    {
        key: "date",
        title: "ERP.DATE",
        kind: "date"
    },
    {
        key: "supplier",
        title: "MENU.SUPPLIERS"
    },
    {
        key: "total",
        title: "ERP.TOTAL",
        align: "end"
    }
];
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

    this.api.getPurchasesReport(fromDate || undefined, toDate || undefined)
      .pipe(finalize(() => { this.loading = false; this.cdr.detectChanges(); }))
      .subscribe({
        next: (data) => {
          this.summary = {
            fromDate: data?.fromDate,
            toDate: data?.toDate,
            invoiceCount: data?.invoiceCount,
            totalPurchases: data?.totalPurchases
          };
          this.rows = (data?.invoices || []).map((row) => {
            const item = row as unknown as Record<string, unknown>;
            return {
              ...item,
              total: Number(item.total || 0).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })
            };
          });
        },
        error: () => {
          this.errorKey = 'COMMON.ERROR_LOADING';
          this.summary = {};
          this.rows = [];
        }
      });
  }
}
