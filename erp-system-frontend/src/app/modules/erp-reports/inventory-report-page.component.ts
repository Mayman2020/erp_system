import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { finalize } from 'rxjs/operators';
import { ErpApiService } from '../../core/services/erp-api.service';

@Component({
  standalone: false,
  selector: 'app-inventory-report-page',
  templateUrl: './inventory-report-page.component.html',
  styleUrls: ['./inventory-report-page.component.scss']
})
export class InventoryReportPageComponent implements OnInit {
  titleKey = 'MENU.INVENTORY_REPORT';
  loading = false;
  errorKey = '';
  summary: Record<string, unknown> = {};
  rows: Array<Record<string, unknown>> = [];
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
  dateFilter = false;

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

    this.api.getInventoryReport()
      .pipe(finalize(() => { this.loading = false; this.cdr.detectChanges(); }))
      .subscribe({
        next: (data) => {
          this.summary = {
            totalSkus: data?.totalSkus,
            lowStockCount: data?.lowStockCount,
            totalQuantity: data?.totalQuantity
          };
          this.rows = (data?.stockLevels || []).map((row) => {
            const item = row as unknown as Record<string, unknown>;
            return {
              ...item,
              quantity: Number(item.quantity || 0).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 }),
              availableQuantity: Number(item.availableQuantity || 0).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })
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
