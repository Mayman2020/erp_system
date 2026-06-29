import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { LowStockAlertDto } from '../../core/models/erp.models';
import { ErpApiService } from '../../core/services/erp-api.service';
import { DataTableColumn } from '../../shared/components/data-table/data-table.component';

@Component({
  standalone: false,
  selector: 'app-low-stock-page',
  templateUrl: './low-stock-page.component.html',
  styleUrls: ['./low-stock-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class LowStockPageComponent implements OnInit {
  readonly titleKey = 'MENU.LOW_STOCK';
  readonly columns: DataTableColumn[] = [
    { key: 'productCode', title: 'ERP.CODE' },
    { key: 'productName', title: 'ERP.PRODUCT' },
    { key: 'totalQuantity', title: 'ERP.AVAILABLE', align: 'end' },
    { key: 'reorderLevel', title: 'ERP.REORDER_LEVEL', align: 'end' },
    { key: 'shortfall', title: 'ERP.SHORTFALL', align: 'end' }
  ];

  rows: Array<Record<string, unknown>> = [];
  loading = false;
  errorKey = '';

  constructor(private api: ErpApiService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.api.getLowStockAlerts().subscribe({
      next: (items) => {
        this.rows = (items || []).map((i) => this.mapRow(i));
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.errorKey = 'COMMON.ERROR_LOADING';
        this.loading = false;
        this.cdr.markForCheck();
      }
    });
  }

  private mapRow(dto: LowStockAlertDto): Record<string, unknown> {
    return {
      ...dto,
      totalQuantity: Number(dto.totalQuantity).toLocaleString(undefined, { minimumFractionDigits: 2 }),
      reorderLevel: Number(dto.reorderLevel).toLocaleString(undefined, { minimumFractionDigits: 2 }),
      shortfall: Number(dto.shortfall).toLocaleString(undefined, { minimumFractionDigits: 2 })
    };
  }
}
