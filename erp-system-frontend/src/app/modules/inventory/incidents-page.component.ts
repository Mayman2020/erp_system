import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { forkJoin, Observable } from 'rxjs';
import { ProductDto, StockIncidentDto, StockIncidentForm, WarehouseDto } from '../../core/models/erp.models';
import { AuthService } from '../../core/auth/auth.service';
import { ConfirmDialogService } from '../../core/services/confirm-dialog.service';
import { ErpApiService } from '../../core/services/erp-api.service';
import { DataTableAction, DataTableColumn } from '../../shared/components/data-table/data-table.component';
import { ErpMasterPageBase, MasterPageConfig, MASTER_CRUD_ACTIONS } from '../../shared/utils/erp-master-page.base';

@Component({
  standalone: false,
  selector: 'app-incidents-page',
  templateUrl: './incidents-page.component.html',
  styleUrls: ['./incidents-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class IncidentsPageComponent extends ErpMasterPageBase<StockIncidentDto, StockIncidentForm> implements OnInit, OnDestroy {
  readonly config: MasterPageConfig = {
    titleKey: 'MENU.STOCK_INCIDENTS',
    createKey: 'INCIDENTS.CREATE',
    editKey: 'COMMON.EDIT',
    viewKey: 'COMMON.VIEW',
    showStatus: true,
    statusOptions: ['DRAFT', 'APPROVED']
  };

  readonly columns: DataTableColumn[] = [
    { key: 'incidentNo', title: 'ERP.NUMBER' },
    { key: 'incidentType', title: 'ERP.TYPE' },
    { key: 'productName', title: 'ERP.PRODUCT' },
    { key: 'warehouseName', title: 'INVOICES.WAREHOUSE' },
    { key: 'quantity', title: 'ERP.QUANTITY', align: 'end' },
    { key: 'financialImpact', title: 'INCIDENTS.FINANCIAL_IMPACT', align: 'end' },
    { key: 'status', title: 'COMMON.STATUS', kind: 'status' }
  ];

  readonly incidentTypes = ['DAMAGED', 'LOST', 'EXPIRED', 'ADJUSTMENT'];

  readonly form = this.fb.group({
    incidentNo: [''],
    warehouseId: [null as number | null, Validators.required],
    productId: [null as number | null, Validators.required],
    quantity: [1, [Validators.required, Validators.min(0.0001)]],
    incidentType: ['DAMAGED', Validators.required],
    reasonCode: [''],
    unitCost: [0],
    notes: ['']
  });

  products: ProductDto[] = [];
  warehouses: WarehouseDto[] = [];

  constructor(
    private api: ErpApiService,
    private fb: FormBuilder,
    authService: AuthService,
    confirmDialog: ConfirmDialogService,
    cdr: ChangeDetectorRef
  ) {
    super(authService, confirmDialog, cdr);
  }

  get tableActions(): DataTableAction[] {
    return [
      ...MASTER_CRUD_ACTIONS.slice(0, 2),
      { id: 'approve', labelKey: 'INCIDENTS.APPROVE', className: 'erp-action-success', disabledWhen: (row) => String(row['status']) !== 'DRAFT' },
      MASTER_CRUD_ACTIONS[2]
    ];
  }

  get productOptions() {
    return [{ id: null, label: '—' }, ...(this.products || []).map((p) => ({ id: p.id, label: `${p.code} - ${p.name || p.nameEn}` }))];
  }

  get warehouseOptions() {
    return [{ id: null, label: '—' }, ...(this.warehouses || []).map((w) => ({ id: w.id, label: `${w.code} - ${w.nameEn || w.name}` }))];
  }

  get incidentTypeOptions() {
    return this.incidentTypes.map((t) => ({ id: t, label: t }));
  }

  ngOnInit(): void {
    forkJoin({ products: this.api.getProducts(), warehouses: this.api.getWarehouses() }).subscribe({
      next: ({ products, warehouses }) => {
        this.products = products || [];
        this.warehouses = warehouses || [];
        this.initMasterPage();
      }
    });
  }

  ngOnDestroy(): void { this.destroyMasterPage(); }

  override onTableAction(event: { actionId: string; row: Record<string, unknown> }): void {
    const id = Number(event.row['id']);
    if (event.actionId === 'approve' && id) {
      this.api.approveStockIncident(id).subscribe({
        next: () => { this.showSuccess('COMMON.APPROVE_SUCCESS'); this.load(); },
        error: (e) => this.showError(e?.error?.message || 'COMMON.UNEXPECTED_ERROR')
      });
      return;
    }
    super.onTableAction(event);
  }

  protected fetchList(filters: Record<string, string>): Observable<StockIncidentDto[]> {
    return this.api.getStockIncidents(filters);
  }

  protected fetchOne(id: number): Observable<StockIncidentDto> {
    return this.api.getStockIncident(id);
  }

  protected createItem(payload: StockIncidentForm): Observable<StockIncidentDto> {
    return this.api.createStockIncident(payload);
  }

  protected updateItem(id: number, payload: StockIncidentForm): Observable<StockIncidentDto> {
    return this.api.updateStockIncident(id, payload);
  }

  protected removeItem(id: number): Observable<void> {
    return this.api.deleteStockIncident(id);
  }

  protected defaultFormValues(): Record<string, unknown> {
    return { incidentNo: '', warehouseId: null, productId: null, quantity: 1, incidentType: 'DAMAGED', reasonCode: '', unitCost: 0, notes: '' };
  }

  protected patchForm(dto: StockIncidentDto): void {
    this.form.patchValue({
      incidentNo: dto.incidentNo,
      warehouseId: dto.warehouseId,
      productId: dto.productId,
      quantity: dto.quantity,
      incidentType: dto.incidentType,
      reasonCode: dto.reasonCode || '',
      unitCost: dto.unitCost || 0,
      notes: dto.notes || ''
    });
  }

  protected toPayload(): StockIncidentForm {
    const v = this.form.getRawValue();
    return {
      incidentNo: v.incidentNo || undefined,
      warehouseId: Number(v.warehouseId),
      productId: Number(v.productId),
      quantity: Number(v.quantity),
      incidentType: v.incidentType!,
      reasonCode: v.reasonCode || undefined,
      unitCost: Number(v.unitCost || 0),
      notes: v.notes || undefined
    };
  }

  protected mapRow(dto: StockIncidentDto): Record<string, unknown> {
    return {
      ...dto,
      quantity: Number(dto.quantity).toLocaleString(undefined, { minimumFractionDigits: 2 }),
      financialImpact: Number(dto.financialImpact || 0).toLocaleString(undefined, { minimumFractionDigits: 2 })
    };
  }
}
