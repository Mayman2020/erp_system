import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { ReplenishmentProposalDto, SupplierDto, WarehouseDto } from '../../core/models/erp.models';
import { ErpApiService } from '../../core/services/erp-api.service';
import { DataTableColumn } from '../../shared/components/data-table/data-table.component';

@Component({
  standalone: false,
  selector: 'app-replenishment-page',
  templateUrl: './replenishment-page.component.html',
  styleUrls: ['./replenishment-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ReplenishmentPageComponent implements OnInit {
  readonly titleKey = 'MENU.REPLENISHMENT';
  readonly columns: DataTableColumn[] = [
    { key: 'productCode', title: 'ERP.CODE' },
    { key: 'productName', title: 'ERP.PRODUCT' },
    { key: 'warehouseName', title: 'INVOICES.WAREHOUSE' },
    { key: 'currentQty', title: 'INVENTORY.CURRENT_QTY', align: 'end' },
    { key: 'reorderLevel', title: 'ERP.REORDER_LEVEL', align: 'end' },
    { key: 'proposedQty', title: 'INVENTORY.PROPOSED_QTY', align: 'end' },
    { key: 'status', title: 'COMMON.STATUS', kind: 'status' }
  ];

  rows: Array<Record<string, unknown>> = [];
  suppliers: SupplierDto[] = [];
  warehouses: WarehouseDto[] = [];
  loading = false;
  generating = false;
  converting = false;
  errorKey = '';
  successKey = '';

  readonly convertForm = this.fb.group({
    supplierId: [null as number | null, Validators.required],
    warehouseId: [null as number | null]
  });

  constructor(private api: ErpApiService, private fb: FormBuilder, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    forkJoin({ suppliers: this.api.getSuppliers(), warehouses: this.api.getWarehouses() }).subscribe({
      next: ({ suppliers, warehouses }) => {
        this.suppliers = suppliers || [];
        this.warehouses = warehouses || [];
        this.load();
      }
    });
  }

  get supplierOptions() {
    return [{ id: null, label: '—' }, ...this.suppliers.map((s) => ({ id: s.id, label: `${s.code} - ${s.nameEn}` }))];
  }

  get warehouseOptions() {
    return [{ id: null, label: '—' }, ...this.warehouses.map((w) => ({ id: w.id, label: `${w.code} - ${w.nameEn || w.name}` }))];
  }

  load(): void {
    this.loading = true;
    this.api.getReplenishmentProposals().subscribe({
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

  generate(): void {
    this.generating = true;
    this.errorKey = '';
    this.api.generateReplenishmentProposals().subscribe({
      next: () => {
        this.successKey = 'INVENTORY.REPLENISH_GENERATED';
        this.generating = false;
        this.load();
        this.cdr.markForCheck();
      },
      error: (e) => {
        this.errorKey = e?.error?.message || 'COMMON.UNEXPECTED_ERROR';
        this.generating = false;
        this.cdr.markForCheck();
      }
    });
  }

  convertToPo(): void {
    if (this.convertForm.invalid) return;
    const v = this.convertForm.getRawValue();
    this.converting = true;
    this.errorKey = '';
    this.api.convertReplenishmentToPurchaseOrder(Number(v.supplierId), v.warehouseId ? Number(v.warehouseId) : undefined).subscribe({
      next: () => {
        this.successKey = 'INVENTORY.REPLENISH_CONVERTED';
        this.converting = false;
        this.load();
        this.cdr.markForCheck();
      },
      error: (e) => {
        this.errorKey = e?.error?.message || 'COMMON.UNEXPECTED_ERROR';
        this.converting = false;
        this.cdr.markForCheck();
      }
    });
  }

  private mapRow(dto: ReplenishmentProposalDto): Record<string, unknown> {
    const fmt = (n: number) => Number(n).toLocaleString(undefined, { minimumFractionDigits: 2 });
    return { ...dto, currentQty: fmt(dto.currentQty), reorderLevel: fmt(dto.reorderLevel), proposedQty: fmt(dto.proposedQty) };
  }
}
