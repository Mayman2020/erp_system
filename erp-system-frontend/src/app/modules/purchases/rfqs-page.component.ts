import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { FormArray, FormBuilder, Validators } from '@angular/forms';
import { Observable } from 'rxjs';
import { ProductDto, PurchaseRfqDto, PurchaseRfqForm } from '../../core/models/erp.models';
import { AuthService } from '../../core/auth/auth.service';
import { ConfirmDialogService } from '../../core/services/confirm-dialog.service';
import { ErpApiService } from '../../core/services/erp-api.service';
import { DataTableAction, DataTableColumn } from '../../shared/components/data-table/data-table.component';
import { ErpMasterPageBase, MasterPageConfig, MASTER_CRUD_ACTIONS } from '../../shared/utils/erp-master-page.base';

@Component({
  standalone: false,
  selector: 'app-rfqs-page',
  templateUrl: './rfqs-page.component.html',
  styleUrls: ['./rfqs-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class RfqsPageComponent extends ErpMasterPageBase<PurchaseRfqDto, PurchaseRfqForm> implements OnInit, OnDestroy {
  readonly config: MasterPageConfig = {
    titleKey: 'MENU.RFQ',
    createKey: 'PURCHASES.CREATE_RFQ',
    editKey: 'COMMON.EDIT',
    viewKey: 'COMMON.VIEW',
    showStatus: true,
    statusOptions: ['DRAFT', 'OPEN']
  };

  readonly columns: DataTableColumn[] = [
    { key: 'rfqNo', title: 'ERP.NUMBER' },
    { key: 'title', title: 'ERP.TITLE' },
    { key: 'dueDate', title: 'PURCHASES.DUE_DATE', kind: 'date' },
    { key: 'status', title: 'COMMON.STATUS', kind: 'status' }
  ];

  readonly form = this.fb.group({
    rfqNo: [''],
    title: ['', Validators.required],
    dueDate: [''],
    notes: [''],
    lines: this.fb.array([this.createLineGroup()])
  });

  products: ProductDto[] = [];

  constructor(
    private api: ErpApiService,
    private fb: FormBuilder,
    authService: AuthService,
    confirmDialog: ConfirmDialogService,
    cdr: ChangeDetectorRef
  ) {
    super(authService, confirmDialog, cdr);
  }

  get lines(): FormArray {
    return this.form.get('lines') as FormArray;
  }

  get tableActions(): DataTableAction[] {
    return [
      ...MASTER_CRUD_ACTIONS.slice(0, 2),
      { id: 'submit', labelKey: 'ERP.SUBMIT', className: 'erp-action-info', disabledWhen: (row) => String(row['status']) !== 'DRAFT' },
      MASTER_CRUD_ACTIONS[2]
    ];
  }

  get productLovItems() {
    return (this.products || []).map((p) => ({ id: p.id, label: `${p.code} - ${p.name || p.nameEn}` }));
  }

  get productOptions() {
    return [{ id: null, label: '—' }, ...this.productLovItems];
  }

  ngOnInit(): void {
    this.api.getProducts().subscribe({
      next: (products) => {
        this.products = products || [];
        this.initMasterPage();
      }
    });
  }

  ngOnDestroy(): void { this.destroyMasterPage(); }

  addLine(): void {
    this.lines.push(this.createLineGroup());
    this.cdr.markForCheck();
  }

  removeLine(index: number): void {
    if (this.lines.length <= 1) return;
    this.lines.removeAt(index);
    this.cdr.markForCheck();
  }

  override onTableAction(event: { actionId: string; row: Record<string, unknown> }): void {
    const id = Number(event.row['id']);
    if (event.actionId === 'submit' && id) {
      this.api.submitPurchaseRfq(id).subscribe({
        next: () => { this.showSuccess('ERP.SUBMIT_SUCCESS'); this.load(); },
        error: (e) => this.showError(e?.error?.message || 'COMMON.UNEXPECTED_ERROR')
      });
      return;
    }
    super.onTableAction(event);
  }

  protected fetchList(filters: Record<string, string>): Observable<PurchaseRfqDto[]> {
    return this.api.getPurchaseRfqs();
  }

  protected fetchOne(id: number): Observable<PurchaseRfqDto> {
    return this.api.getPurchaseRfq(id);
  }

  protected createItem(payload: PurchaseRfqForm): Observable<PurchaseRfqDto> {
    return this.api.createPurchaseRfq(payload);
  }

  protected updateItem(id: number, payload: PurchaseRfqForm): Observable<PurchaseRfqDto> {
    return this.api.updatePurchaseRfq(id, payload);
  }

  protected removeItem(id: number): Observable<void> {
    return this.api.deletePurchaseRfq(id);
  }

  protected defaultFormValues(): Record<string, unknown> {
    this.lines.clear();
    this.lines.push(this.createLineGroup());
    return { rfqNo: '', title: '', dueDate: '', notes: '' };
  }

  protected patchForm(dto: PurchaseRfqDto): void {
    this.form.patchValue({ rfqNo: dto.rfqNo, title: dto.title, dueDate: dto.dueDate || '', notes: dto.notes || '' });
    this.lines.clear();
    (dto.lines || [{ productId: null, quantity: 1 }]).forEach((line) => {
      this.lines.push(this.fb.group({
        productId: [line.productId, Validators.required],
        quantity: [line.quantity, [Validators.required, Validators.min(0.0001)]],
        notes: [line.notes || '']
      }));
    });
  }

  protected toPayload(): PurchaseRfqForm {
    const v = this.form.getRawValue();
    return {
      rfqNo: v.rfqNo || undefined,
      title: v.title!,
      dueDate: v.dueDate || undefined,
      notes: v.notes || undefined,
      lines: (v.lines || []).map((line: Record<string, unknown>) => ({
        productId: Number(line['productId']),
        quantity: Number(line['quantity']),
        notes: String(line['notes'] || '') || undefined
      }))
    };
  }

  protected mapRow(dto: PurchaseRfqDto): Record<string, unknown> {
    return { ...dto };
  }

  private createLineGroup() {
    return this.fb.group({
      productId: [null as number | null, Validators.required],
      quantity: [1, [Validators.required, Validators.min(0.0001)]],
      notes: ['']
    });
  }
}
