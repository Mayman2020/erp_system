import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { finalize } from 'rxjs/operators';
import { ErpApiService } from '../../core/services/erp-api.service';

@Component({
  standalone: false,
  selector: 'app-stock-levels-page',
  templateUrl: './stock-levels-page.component.html',
  styleUrls: ['./stock-levels-page.component.scss']
})
export class StockLevelsPageComponent implements OnInit {
  titleKey = 'MENU.STOCK_LEVELS';
  loading = false;
  errorKey = '';
  rows: Array<Record<string, unknown>> = [];
  statusOptions: string[] = ["DRAFT","APPROVED","CANCELLED"];
  columns = [
    {
        key: "productCode",
        title: "ERP.PRODUCT"
    },
    {
        key: "productName",
        title: "COMMON.NAME"
    },
    {
        key: "warehouseName",
        title: "MENU.WAREHOUSES"
    },
    {
        key: "quantity",
        title: "ERP.QUANTITY",
        align: "end"
    },
    {
        key: "availableQuantity",
        title: "ERP.AVAILABLE",
        align: "end"
    }
];
  showDateRange = false;
  showStatus = false;

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
    const params: Record<string, string> = { search: this.filters.query || '' };
    if (this.showStatus && this.filters.status) {
      params.status = this.filters.status;
    }
    if (this.showDateRange) {
      if (this.filters.fromDate) params.fromDate = this.filters.fromDate;
      if (this.filters.toDate) params.toDate = this.filters.toDate;
    }

    this.api.getStockLevels(params)
      .pipe(finalize(() => { this.loading = false; this.cdr.detectChanges(); }))
      .subscribe({
        next: (rows) => {
          this.rows = (rows || []).map((row) => this.mapRow(row as unknown as Record<string, unknown>));
        },
        error: () => {
          this.errorKey = 'COMMON.ERROR_LOADING';
          this.rows = [];
        }
      });
  }

  private mapRow(row: Record<string, unknown>): Record<string, unknown> {
    const mapped = { ...row };
    ['totalAmount', 'subtotal', 'taxAmount', 'discountAmount', 'paidAmount', 'remainingAmount', 'amount', 'quantity', 'availableQuantity', 'costPrice', 'salePrice', 'budget', 'basicSalary', 'totalSales', 'totalPurchases', 'netProfit', 'totalQuantity'].forEach((key) => {
      if (mapped[key] !== undefined && mapped[key] !== null && typeof mapped[key] === 'number') {
        mapped[key] = Number(mapped[key]).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 });
      }
    });
    return mapped;
  }
}
