import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { forkJoin, Observable, throwError } from 'rxjs';
import { AccountDto, AccountingCheckDto, AccountingCheckForm, BankAccountDto } from '../../core/models/accounting.models';
import { AuthService } from '../../core/auth/auth.service';
import { AccountingApiService } from '../../core/services/accounting-api.service';
import { ConfirmDialogService } from '../../core/services/confirm-dialog.service';
import { DataTableAction, DataTableColumn } from '../../shared/components/data-table/data-table.component';
import { ErpMasterPageBase, MasterPageConfig, MASTER_CRUD_ACTIONS } from '../../shared/utils/erp-master-page.base';

@Component({
  standalone: false,
  selector: 'app-checks-page',
  templateUrl: './checks-page.component.html',
  styleUrls: ['./checks-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ChecksPageComponent extends ErpMasterPageBase<AccountingCheckDto, AccountingCheckForm> implements OnInit, OnDestroy {
  readonly config: MasterPageConfig = {
    titleKey: 'NAV.CHECKS',
    createKey: 'CHECKS.CREATE',
    editKey: 'COMMON.EDIT',
    viewKey: 'COMMON.VIEW',
    showStatus: true,
    showDateRange: true,
    statusOptions: ['PENDING', 'DEPOSITED', 'CLEARED', 'BOUNCED', 'CANCELLED']
  };

  readonly columns: DataTableColumn[] = [
    { key: 'checkNumber', title: 'CHECKS.NUMBER', align: 'start' },
    { key: 'issueDate', title: 'CHECKS.ISSUE_DATE', kind: 'date' },
    { key: 'dueDate', title: 'CHECKS.DUE_DATE', kind: 'date' },
    { key: 'amount', title: 'CHECKS.AMOUNT', align: 'end' },
    { key: 'status', title: 'COMMON.STATUS', kind: 'status' },
    { key: 'checkType', title: 'CHECKS.TYPE', kind: 'type', prefix: 'CHECK_TYPE.' },
    { key: 'bankName', title: 'CHECKS.BANK', align: 'start' },
    { key: 'partyName', title: 'CHECKS.PARTY', align: 'start' }
  ];

  readonly form = this.fb.group({
    checkNumber: [''],
    checkType: ['RECEIVED', Validators.required],
    issueDate: [new Date().toISOString().slice(0, 10), Validators.required],
    dueDate: [new Date().toISOString().slice(0, 10), Validators.required],
    bankName: ['', Validators.required],
    amount: [0, [Validators.required, Validators.min(0.01)]],
    partyName: [''],
    linkedDocumentReference: [''],
    bankAccountId: [null as number | null, Validators.required],
    holdingAccountId: [null as number | null, Validators.required]
  });

  bankAccounts: BankAccountDto[] = [];
  assetAccounts: AccountDto[] = [];
  checkTypes = ['ISSUED', 'RECEIVED'];
  editingRecord: AccountingCheckDto | null = null;

  constructor(
    private api: AccountingApiService,
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
      { id: 'deposit', labelKey: 'CHECKS.DEPOSIT', className: 'erp-action-info', disabledWhen: (r) => String(r['status']) !== 'PENDING' },
      { id: 'clear', labelKey: 'CHECKS.CLEAR', className: 'erp-action-success', disabledWhen: (r) => String(r['status']) !== 'DEPOSITED' },
      { id: 'bounce', labelKey: 'CHECKS.BOUNCE', className: 'erp-action-warning', disabledWhen: (r) => !['PENDING', 'DEPOSITED'].includes(String(r['status'])) },
      { id: 'cancel', labelKey: 'COMMON.CANCEL', className: 'erp-action-danger', disabledWhen: (r) => String(r['status']) === 'CANCELLED' || String(r['status']) === 'CLEARED' }
    ];
  }

  get bankAccountOptions() {
    return [{ id: null, label: '—' }, ...(this.bankAccounts || []).map((b) => ({ id: b.id, label: `${b.bankName} (${b.accountNumber})` }))];
  }

  get accountOptions() {
    return [{ id: null, label: '—' }, ...(this.assetAccounts || []).map((a) => ({ id: a.id, label: `${a.code} - ${a.nameEn || a.name}` }))];
  }

  ngOnInit(): void {
    forkJoin({
      banks: this.api.getBankAccounts({ active: true }),
      accounts: this.api.getAccounts({ type: 'ASSET', active: true })
    }).subscribe({
      next: ({ banks, accounts }) => {
        this.bankAccounts = banks || [];
        this.assetAccounts = accounts || [];
        this.initMasterPage();
      },
      error: () => this.initMasterPage()
    });
  }

  ngOnDestroy(): void { this.destroyMasterPage(); }

  override onTableAction(event: { actionId: string; row: Record<string, unknown> }): void {
    const id = Number(event.row['id']);
    if (!id) {
      return;
    }
    if (event.actionId === 'deposit') {
      this.api.depositCheck(id, this.actorEmail).subscribe({ next: () => { this.showSuccess('CHECKS.DEPOSIT_SUCCESS'); this.load(); } });
      return;
    }
    if (event.actionId === 'clear') {
      this.api.clearCheck(id, this.actorEmail).subscribe({ next: () => { this.showSuccess('CHECKS.CLEAR_SUCCESS'); this.load(); } });
      return;
    }
    if (event.actionId === 'bounce') {
      this.api.bounceCheck(id, this.actorEmail).subscribe({ next: () => { this.showSuccess('CHECKS.BOUNCE_SUCCESS'); this.load(); } });
      return;
    }
    if (event.actionId === 'cancel') {
      this.api.cancelCheck(id, this.actorEmail).subscribe({ next: () => { this.showSuccess('COMMON.CANCEL_SUCCESS'); this.load(); } });
      return;
    }
    super.onTableAction(event);
  }

  protected fetchList(filters: Record<string, string>): Observable<AccountingCheckDto[]> {
    return this.api.getChecks(filters);
  }

  protected fetchOne(id: number): Observable<AccountingCheckDto> {
    return this.api.getCheck(id);
  }

  protected createItem(payload: AccountingCheckForm): Observable<AccountingCheckDto> {
    return this.api.createCheck(payload);
  }

  protected updateItem(id: number, payload: AccountingCheckForm): Observable<AccountingCheckDto> {
    return this.api.updateCheck(id, payload);
  }

  protected removeItem(_id: number): Observable<void> {
    return throwError(() => new Error('Checks cannot be deleted; use Cancel instead.'));
  }

  protected defaultFormValues(): Record<string, unknown> {
    const today = new Date().toISOString().slice(0, 10);
    return {
      checkNumber: '',
      checkType: 'RECEIVED',
      issueDate: today,
      dueDate: today,
      bankName: '',
      amount: 0,
      partyName: '',
      linkedDocumentReference: '',
      bankAccountId: null,
      holdingAccountId: null
    };
  }

  protected patchForm(dto: AccountingCheckDto): void {
    this.editingRecord = dto;
    this.form.reset({
      checkNumber: dto.checkNumber || '',
      checkType: dto.checkType || 'RECEIVED',
      issueDate: dto.issueDate || '',
      dueDate: dto.dueDate || '',
      bankName: dto.bankName || '',
      amount: Number(dto.amount || 0),
      partyName: dto.partyName || '',
      linkedDocumentReference: dto.linkedDocumentReference || '',
      bankAccountId: dto.bankAccountId || null,
      holdingAccountId: dto.holdingAccountId || null
    });
  }

  protected toPayload(): AccountingCheckForm {
    const v = this.form.getRawValue();
    return {
      checkNumber: v.checkNumber || undefined,
      checkType: v.checkType!,
      issueDate: v.issueDate!,
      dueDate: v.dueDate!,
      bankName: v.bankName!,
      amount: Number(v.amount),
      partyName: v.partyName || undefined,
      linkedDocumentReference: v.linkedDocumentReference || undefined,
      bankAccountId: Number(v.bankAccountId),
      holdingAccountId: Number(v.holdingAccountId)
    };
  }

  protected mapRow(dto: AccountingCheckDto): Record<string, unknown> {
    return {
      ...dto,
      amount: Number(dto.amount || 0).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })
    };
  }
}
