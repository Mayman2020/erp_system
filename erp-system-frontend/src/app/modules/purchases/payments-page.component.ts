import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { forkJoin, Observable } from 'rxjs';
import { PurchaseInvoiceDto, SupplierDto, SupplierPaymentDto, SupplierPaymentForm } from '../../core/models/erp.models';
import { AuthService } from '../../core/auth/auth.service';
import { ConfirmDialogService } from '../../core/services/confirm-dialog.service';
import { ErpApiService } from '../../core/services/erp-api.service';
import { DataTableAction, DataTableColumn } from '../../shared/components/data-table/data-table.component';
import { ErpMasterPageBase, MasterPageConfig, MASTER_CRUD_ACTIONS } from '../../shared/utils/erp-master-page.base';

@Component({
  standalone: false,
  selector: 'app-payments-page',
  templateUrl: './payments-page.component.html',
  styleUrls: ['./payments-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PaymentsPageComponent extends ErpMasterPageBase<SupplierPaymentDto, SupplierPaymentForm> implements OnInit, OnDestroy {
  readonly config: MasterPageConfig = {
    titleKey: 'MENU.SUPPLIER_PAYMENTS',
    createKey: 'ERP.CREATE_PAYMENT',
    editKey: 'COMMON.EDIT',
    viewKey: 'COMMON.VIEW',
    showStatus: true,
    statusOptions: ['DRAFT', 'APPROVED', 'CANCELLED']
  };

  readonly columns: DataTableColumn[] = [
    { key: 'paymentNumber', title: 'ERP.NUMBER' },
    { key: 'paymentDate', title: 'COMMON.DATE', kind: 'date' },
    { key: 'supplierName', title: 'MENU.SUPPLIERS' },
    { key: 'amount', title: 'ERP.AMOUNT', align: 'end' },
    { key: 'paymentMethod', title: 'ERP.PAYMENT_METHOD' },
    { key: 'status', title: 'COMMON.STATUS', kind: 'status' }
  ];

  readonly form = this.fb.group({
    paymentNumber: [''],
    paymentDate: [new Date().toISOString().slice(0, 10), Validators.required],
    supplierId: [null as number | null, Validators.required],
    invoiceId: [null as number | null],
    amount: [0, [Validators.required, Validators.min(0.01)]],
    paymentMethod: ['CASH', Validators.required],
    notes: ['']
  });

  suppliers: SupplierDto[] = [];
  invoices: PurchaseInvoiceDto[] = [];

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
      { id: 'approve', labelKey: 'COMMON.APPROVE', className: 'erp-action-success', disabledWhen: (r) => String(r['status']) !== 'DRAFT' },
      { id: 'cancel', labelKey: 'COMMON.CANCEL', className: 'erp-action-warning', disabledWhen: (r) => ['APPROVED', 'CANCELLED'].includes(String(r['status'])) },
      MASTER_CRUD_ACTIONS[2]
    ];
  }

  get supplierOptions() {
    return [{ id: null, label: '—' }, ...(this.suppliers || []).map((s) => ({ id: s.id, label: `${s.code} - ${s.nameEn}` }))];
  }

  get invoiceOptions() {
    return [{ id: null, label: '—' }, ...(this.invoices || []).map((i) => ({ id: i.id, label: `${i.invoiceNumber} (${i.totalAmount})` }))];
  }

  ngOnInit(): void {
    forkJoin({ suppliers: this.api.getSuppliers(), invoices: this.api.getPurchaseInvoices() }).subscribe({
      next: ({ suppliers, invoices }) => {
        this.suppliers = suppliers || [];
        this.invoices = invoices || [];
        this.initMasterPage();
      }
    });
  }

  ngOnDestroy(): void { this.destroyMasterPage(); }

  override onTableAction(event: { actionId: string; row: Record<string, unknown> }): void {
    const id = Number(event.row['id']);
    if (event.actionId === 'approve' && id) {
      this.api.approveSupplierPayment(id, this.actorEmail).subscribe({ next: () => { this.showSuccess('COMMON.APPROVE_SUCCESS'); this.load(); } });
      return;
    }
    if (event.actionId === 'cancel' && id) {
      this.api.cancelSupplierPayment(id, this.actorEmail).subscribe({ next: () => { this.showSuccess('COMMON.CANCEL_SUCCESS'); this.load(); } });
      return;
    }
    super.onTableAction(event);
  }

  protected fetchList(filters: Record<string, string>): Observable<SupplierPaymentDto[]> {
    return this.api.getSupplierPayments(filters);
  }

  protected fetchOne(id: number): Observable<SupplierPaymentDto> {
    return this.api.getSupplierPayment(id);
  }

  protected createItem(payload: SupplierPaymentForm): Observable<SupplierPaymentDto> {
    return this.api.createSupplierPayment(payload);
  }

  protected updateItem(id: number, payload: SupplierPaymentForm): Observable<SupplierPaymentDto> {
    return this.api.updateSupplierPayment(id, payload);
  }

  protected removeItem(id: number): Observable<void> {
    return this.api.deleteSupplierPayment(id);
  }

  protected defaultFormValues(): Record<string, unknown> {
    return { paymentNumber: '', paymentDate: new Date().toISOString().slice(0, 10), supplierId: null, invoiceId: null, amount: 0, paymentMethod: 'CASH', notes: '' };
  }

  protected patchForm(dto: SupplierPaymentDto): void {
    this.form.patchValue({ paymentNumber: dto.paymentNumber, paymentDate: dto.paymentDate, supplierId: dto.supplierId, invoiceId: dto.invoiceId || null, amount: dto.amount, paymentMethod: dto.paymentMethod || 'CASH', notes: dto.notes || '' });
  }

  protected toPayload(): SupplierPaymentForm {
    const v = this.form.getRawValue();
    return { paymentNumber: v.paymentNumber || undefined, paymentDate: v.paymentDate!, supplierId: Number(v.supplierId), invoiceId: v.invoiceId ? Number(v.invoiceId) : undefined, amount: Number(v.amount), paymentMethod: v.paymentMethod!, notes: v.notes || undefined };
  }

  protected mapRow(dto: SupplierPaymentDto): Record<string, unknown> {
    return { ...dto, amount: Number(dto.amount).toLocaleString(undefined, { minimumFractionDigits: 2 }) };
  }
}
