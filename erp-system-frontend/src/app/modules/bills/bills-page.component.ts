import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { FormArray, FormBuilder, Validators } from '@angular/forms';
import { forkJoin, Observable, throwError } from 'rxjs';
import { AccountDto, BillDto, BillForm } from '../../core/models/accounting.models';
import { AuthService } from '../../core/auth/auth.service';
import { AccountingApiService } from '../../core/services/accounting-api.service';
import { ConfirmDialogService } from '../../core/services/confirm-dialog.service';
import { DataTableAction, DataTableColumn } from '../../shared/components/data-table/data-table.component';
import { ErpMasterPageBase, MasterPageConfig, MASTER_CRUD_ACTIONS } from '../../shared/utils/erp-master-page.base';

@Component({
  standalone: false,
  selector: 'app-bills-page',
  templateUrl: './bills-page.component.html',
  styleUrls: ['./bills-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class BillsPageComponent extends ErpMasterPageBase<BillDto, BillForm> implements OnInit, OnDestroy {
  readonly config: MasterPageConfig = {
    titleKey: 'BILLS.TITLE',
    createKey: 'BILLS.CREATE',
    editKey: 'COMMON.EDIT',
    viewKey: 'COMMON.VIEW',
    showStatus: true,
    statusOptions: ['DRAFT', 'APPROVED', 'POSTED', 'PARTIALLY_PAID', 'PAID', 'CANCELLED']
  };

  readonly columns: DataTableColumn[] = [
    { key: 'billNumber', title: 'BILLS.NUMBER', align: 'start' },
    { key: 'billDate', title: 'BILLS.DATE', kind: 'date' },
    { key: 'supplierName', title: 'INVOICES.SUPPLIER', align: 'start' },
    { key: 'totalAmount', title: 'INVOICES.TOTAL', align: 'end' },
    { key: 'status', title: 'COMMON.STATUS', kind: 'status' },
    { key: 'outstandingAmount', title: 'INVOICES.OUTSTANDING', align: 'end' }
  ];

  readonly form = this.fb.group({
    billNumber: [''],
    billDate: [new Date().toISOString().slice(0, 10), Validators.required],
    dueDate: [new Date().toISOString().slice(0, 10), Validators.required],
    supplierName: [''],
    supplierReference: [''],
    description: [''],
    payableAccountId: [null as number | null, Validators.required],
    taxAccountId: [null as number | null],
    taxAmount: [0, [Validators.required, Validators.min(0)]],
    lines: this.fb.array([this.createLineGroup()])
  });

  liabilityAccounts: AccountDto[] = [];
  expenseAccounts: AccountDto[] = [];
  editingRecord: BillDto | null = null;

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

  get payableOptions() {
    return [{ id: null, label: '—' }, ...(this.liabilityAccounts || []).map((a) => ({ id: a.id, label: `${a.code} - ${a.nameEn || a.name}` }))];
  }

  get lineAccountOptions() {
    return [{ id: null, label: '—' }, ...(this.expenseAccounts || []).map((a) => ({ id: a.id, label: `${a.code} - ${a.nameEn || a.name}` }))];
  }

  ngOnInit(): void {
    forkJoin({
      liabilities: this.api.getAccounts({ type: 'LIABILITY', active: true }),
      expenses: this.api.getAccounts({ type: 'EXPENSE', active: true })
    }).subscribe({
      next: ({ liabilities, expenses }) => {
        this.liabilityAccounts = liabilities || [];
        this.expenseAccounts = expenses || [];
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
      this.api.approveBill(id, this.actorEmail).subscribe({ next: () => { this.showSuccess('BILLS.APPROVE_SUCCESS'); this.load(); } });
      return;
    }
    if (event.actionId === 'cancel') {
      this.api.cancelBill(id, this.actorEmail).subscribe({ next: () => { this.showSuccess('BILLS.CANCEL_SUCCESS'); this.load(); } });
      return;
    }
    super.onTableAction(event);
  }

  protected fetchList(filters: Record<string, string>): Observable<BillDto[]> {
    return this.api.getBills(filters);
  }

  protected fetchOne(id: number): Observable<BillDto> {
    return this.api.getBill(id);
  }

  protected createItem(payload: BillForm): Observable<BillDto> {
    return this.api.createBill(payload);
  }

  protected updateItem(id: number, payload: BillForm): Observable<BillDto> {
    return this.api.updateBill(id, payload);
  }

  protected removeItem(_id: number): Observable<void> {
    return throwError(() => new Error('Bills cannot be deleted; use Cancel instead.'));
  }

  protected defaultFormValues(): Record<string, unknown> {
    const today = new Date().toISOString().slice(0, 10);
    return {
      billNumber: '',
      billDate: today,
      dueDate: today,
      supplierName: '',
      supplierReference: '',
      description: '',
      payableAccountId: null,
      taxAccountId: null,
      taxAmount: 0,
      lines: [{ accountId: null, description: '', quantity: 1, unitPrice: 0 }]
    };
  }

  protected patchForm(dto: BillDto): void {
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
      billNumber: dto.billNumber || '',
      billDate: dto.billDate || '',
      dueDate: dto.dueDate || '',
      supplierName: dto.supplierName || '',
      supplierReference: dto.supplierReference || '',
      description: dto.description || '',
      payableAccountId: dto.payableAccountId || null,
      taxAccountId: dto.taxAccountId || null,
      taxAmount: Number(dto.taxAmount || 0)
    });
  }

  protected toPayload(): BillForm {
    const v = this.form.getRawValue();
    return {
      billNumber: v.billNumber || undefined,
      billDate: v.billDate!,
      dueDate: v.dueDate!,
      supplierName: v.supplierName || undefined,
      supplierReference: v.supplierReference || undefined,
      description: v.description || undefined,
      payableAccountId: Number(v.payableAccountId),
      taxAccountId: v.taxAccountId ? Number(v.taxAccountId) : null,
      taxAmount: Number(v.taxAmount),
      lines: (v.lines || []).map((line: { accountId: number; description?: string; quantity: number; unitPrice: number }) => ({
        accountId: Number(line.accountId),
        description: line.description || undefined,
        quantity: Number(line.quantity),
        unitPrice: Number(line.unitPrice)
      }))
    };
  }

  protected mapRow(dto: BillDto): Record<string, unknown> {
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
