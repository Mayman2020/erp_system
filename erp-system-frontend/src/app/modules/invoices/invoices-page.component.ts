import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { FormArray, FormBuilder, Validators } from '@angular/forms';
import { forkJoin, Observable, throwError } from 'rxjs';
import { AccountDto, CustomerInvoiceDto, CustomerInvoiceForm } from '../../core/models/accounting.models';
import { AuthService } from '../../core/auth/auth.service';
import { AccountingApiService } from '../../core/services/accounting-api.service';
import { ConfirmDialogService } from '../../core/services/confirm-dialog.service';
import { DataTableAction, DataTableColumn } from '../../shared/components/data-table/data-table.component';
import { ErpMasterPageBase, MasterPageConfig, MASTER_CRUD_ACTIONS } from '../../shared/utils/erp-master-page.base';

@Component({
  standalone: false,
  selector: 'app-invoices-page',
  templateUrl: './invoices-page.component.html',
  styleUrls: ['./invoices-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class InvoicesPageComponent extends ErpMasterPageBase<CustomerInvoiceDto, CustomerInvoiceForm> implements OnInit, OnDestroy {
  readonly config: MasterPageConfig = {
    titleKey: 'NAV.INVOICES',
    createKey: 'INVOICES.CREATE',
    editKey: 'COMMON.EDIT',
    viewKey: 'COMMON.VIEW',
    saveSuccessKey: 'INVOICES.SAVE_SUCCESS',
    showStatus: true,
    showDateRange: true,
    statusOptions: ['DRAFT', 'POSTED', 'PARTIAL', 'PAID', 'CANCELLED']
  };

  readonly columns: DataTableColumn[] = [
    { key: 'invoiceNumber', title: 'INVOICES.NUMBER', align: 'start' },
    { key: 'invoiceDate', title: 'INVOICES.DATE', kind: 'date' },
    { key: 'customerName', title: 'INVOICES.CUSTOMER', align: 'start' },
    { key: 'totalAmount', title: 'INVOICES.TOTAL', align: 'end' },
    { key: 'status', title: 'COMMON.STATUS', kind: 'status' },
    { key: 'outstandingAmount', title: 'INVOICES.OUTSTANDING', align: 'end' }
  ];

  readonly form = this.fb.group({
    invoiceNumber: [''],
    invoiceDate: [new Date().toISOString().slice(0, 10), Validators.required],
    dueDate: [new Date().toISOString().slice(0, 10), Validators.required],
    customerName: [''],
    customerReference: [''],
    description: [''],
    receivableAccountId: [null as number | null, Validators.required],
    revenueAccountId: [null as number | null, Validators.required],
    taxAmount: [0, [Validators.required, Validators.min(0)]],
    lines: this.fb.array([this.createLineGroup()])
  });

  assetAccounts: AccountDto[] = [];
  revenueAccounts: AccountDto[] = [];
  editingRecord: CustomerInvoiceDto | null = null;

  constructor(
    private api: AccountingApiService,
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
      { id: 'approve', labelKey: 'COMMON.APPROVE', className: 'erp-action-success', disabledWhen: (r) => String(r['status']) !== 'DRAFT' },
      { id: 'cancel', labelKey: 'COMMON.CANCEL', className: 'erp-action-warning', disabledWhen: (r) => ['CANCELLED', 'PAID'].includes(String(r['status'])) }
    ];
  }

  get receivableOptions() {
    return [{ id: null, label: '—' }, ...(this.assetAccounts || []).map((a) => ({ id: a.id, label: `${a.code} - ${a.nameEn || a.name}` }))];
  }

  get revenueOptions() {
    return [{ id: null, label: '—' }, ...(this.revenueAccounts || []).map((a) => ({ id: a.id, label: `${a.code} - ${a.nameEn || a.name}` }))];
  }

  get lineAccountOptions() {
    return [{ id: null, label: '—' }, ...(this.revenueAccounts || []).map((a) => ({ id: a.id, label: `${a.code} - ${a.nameEn || a.name}` }))];
  }

  ngOnInit(): void {
    forkJoin({
      assets: this.api.getAccounts({ type: 'ASSET', active: true }),
      revenues: this.api.getAccounts({ type: 'REVENUE', active: true })
    }).subscribe({
      next: ({ assets, revenues }) => {
        this.assetAccounts = assets || [];
        this.revenueAccounts = revenues || [];
        this.initMasterPage();
      },
      error: () => this.initMasterPage()
    });
  }

  ngOnDestroy(): void { this.destroyMasterPage(); }

  addLine(): void {
    this.lines.push(this.createLineGroup());
    this.cdr.markForCheck();
  }

  removeLine(index: number): void {
    if (this.lines.length > 1) {
      this.lines.removeAt(index);
      this.cdr.markForCheck();
    }
  }

  override onTableAction(event: { actionId: string; row: Record<string, unknown> }): void {
    const id = Number(event.row['id']);
    if (!id) {
      return;
    }
    if (event.actionId === 'approve') {
      this.api.approveInvoice(id, this.actorEmail).subscribe({ next: () => { this.showSuccess('INVOICES.APPROVE_SUCCESS'); this.load(); } });
      return;
    }
    if (event.actionId === 'cancel') {
      this.api.cancelInvoice(id, this.actorEmail).subscribe({ next: () => { this.showSuccess('INVOICES.CANCEL_SUCCESS'); this.load(); } });
      return;
    }
    super.onTableAction(event);
  }

  protected fetchList(filters: Record<string, string>): Observable<CustomerInvoiceDto[]> {
    return this.api.getInvoices(filters);
  }

  protected fetchOne(id: number): Observable<CustomerInvoiceDto> {
    return this.api.getInvoice(id);
  }

  protected createItem(payload: CustomerInvoiceForm): Observable<CustomerInvoiceDto> {
    return this.api.createInvoice(payload);
  }

  protected updateItem(id: number, payload: CustomerInvoiceForm): Observable<CustomerInvoiceDto> {
    return this.api.updateInvoice(id, payload);
  }

  protected removeItem(_id: number): Observable<void> {
    return throwError(() => new Error('Invoices cannot be deleted; use Cancel instead.'));
  }

  protected defaultFormValues(): Record<string, unknown> {
    const today = new Date().toISOString().slice(0, 10);
    return {
      invoiceNumber: '',
      invoiceDate: today,
      dueDate: today,
      customerName: '',
      customerReference: '',
      description: '',
      receivableAccountId: null,
      revenueAccountId: null,
      taxAmount: 0,
      lines: [{ accountId: null, description: '', quantity: 1, unitPrice: 0 }]
    };
  }

  protected patchForm(dto: CustomerInvoiceDto): void {
    this.editingRecord = dto;
    while (this.lines.length) {
      this.lines.removeAt(0);
    }
    (dto.lines || [{ accountId: null, description: '', quantity: 1, unitPrice: 0 }]).forEach((line) => {
      this.lines.push(this.fb.group({
        accountId: [line.accountId || null, Validators.required],
        description: [line.description || ''],
        quantity: [Number(line.quantity || 1), [Validators.required, Validators.min(0.01)]],
        unitPrice: [Number(line.unitPrice || 0), [Validators.required, Validators.min(0)]]
      }));
    });
    this.form.patchValue({
      invoiceNumber: dto.invoiceNumber || '',
      invoiceDate: dto.invoiceDate || '',
      dueDate: dto.dueDate || '',
      customerName: dto.customerName || '',
      customerReference: dto.customerReference || '',
      description: dto.description || '',
      receivableAccountId: dto.receivableAccountId || null,
      revenueAccountId: dto.revenueAccountId || null,
      taxAmount: Number(dto.taxAmount || 0)
    });
  }

  protected toPayload(): CustomerInvoiceForm {
    const v = this.form.getRawValue();
    return {
      invoiceNumber: v.invoiceNumber || undefined,
      invoiceDate: v.invoiceDate!,
      dueDate: v.dueDate!,
      customerName: v.customerName || undefined,
      customerReference: v.customerReference || undefined,
      description: v.description || undefined,
      receivableAccountId: Number(v.receivableAccountId),
      revenueAccountId: Number(v.revenueAccountId),
      taxAmount: Number(v.taxAmount),
      lines: (v.lines || []).map((line: { accountId: number; description?: string; quantity: number; unitPrice: number }) => ({
        accountId: Number(line.accountId),
        description: line.description || undefined,
        quantity: Number(line.quantity),
        unitPrice: Number(line.unitPrice)
      }))
    };
  }

  protected mapRow(dto: CustomerInvoiceDto): Record<string, unknown> {
    return {
      ...dto,
      totalAmount: Number(dto.totalAmount || 0).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 }),
      outstandingAmount: Number(dto.outstandingAmount || 0).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })
    };
  }

  private createLineGroup() {
    return this.fb.group({
      accountId: [null as number | null, Validators.required],
      description: [''],
      quantity: [1, [Validators.required, Validators.min(0.01)]],
      unitPrice: [0, [Validators.required, Validators.min(0)]]
    });
  }
}
