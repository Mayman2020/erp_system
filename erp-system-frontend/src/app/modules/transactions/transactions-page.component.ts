import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { forkJoin, Observable, of } from 'rxjs';
import { AccountDto, AccountingTransactionDto, AccountingTransactionForm } from '../../core/models/accounting.models';
import { AuthService } from '../../core/auth/auth.service';
import { AccountingApiService } from '../../core/services/accounting-api.service';
import { ConfirmDialogService } from '../../core/services/confirm-dialog.service';
import { LookupService } from '../../core/services/lookup.service';
import { DataTableAction, DataTableColumn } from '../../shared/components/data-table/data-table.component';
import { ErpMasterPageBase, MasterPageConfig, MASTER_CRUD_ACTIONS } from '../../shared/utils/erp-master-page.base';

@Component({
  standalone: false,
  selector: 'app-transactions-page',
  templateUrl: './transactions-page.component.html',
  styleUrls: ['./transactions-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TransactionsPageComponent extends ErpMasterPageBase<AccountingTransactionDto, AccountingTransactionForm> implements OnInit, OnDestroy {
  readonly config: MasterPageConfig = {
    titleKey: 'NAV.TRANSACTIONS',
    createKey: 'TRANSACTIONS.CREATE',
    editKey: 'COMMON.EDIT',
    viewKey: 'COMMON.VIEW',
    showStatus: true,
    showDateRange: true,
    statusOptions: ['DRAFT', 'APPROVED', 'CANCELLED']
  };

  readonly columns: DataTableColumn[] = [
    { key: 'reference', title: 'TRANSACTIONS.REFERENCE', align: 'start' },
    { key: 'transactionDate', title: 'TRANSACTIONS.DATE', kind: 'date' },
    { key: 'transactionType', title: 'TRANSACTIONS.TYPE', kind: 'type', prefix: 'TRANSACTION_TYPE.' },
    { key: 'amount', title: 'TRANSACTIONS.AMOUNT', align: 'end' },
    { key: 'status', title: 'COMMON.STATUS', kind: 'status' },
    { key: 'debitAccountName', title: 'TRANSACTIONS.DEBIT_ACCOUNT', align: 'start' },
    { key: 'creditAccountName', title: 'TRANSACTIONS.CREDIT_ACCOUNT', align: 'start' }
  ];

  readonly form = this.fb.group({
    transactionDate: [new Date().toISOString().slice(0, 10), Validators.required],
    reference: [''],
    description: [''],
    transactionType: ['JOURNAL_VOUCHER', Validators.required],
    amount: [0, [Validators.required, Validators.min(0.01)]],
    debitAccountId: [null as number | null, Validators.required],
    creditAccountId: [null as number | null, Validators.required],
    relatedDocumentReference: ['']
  });

  accounts: AccountDto[] = [];
  transactionTypes: string[] = [];

  constructor(
    private api: AccountingApiService,
    private lookupService: LookupService,
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
      { id: 'cancel', labelKey: 'COMMON.CANCEL', className: 'erp-action-warning', disabledWhen: (r) => String(r['status']) === 'CANCELLED' }
    ];
  }

  get accountOptions() {
    return [{ id: null, label: '—' }, ...(this.accounts || []).map((a) => ({ id: a.id, label: `${a.code} - ${a.nameEn || a.name}` }))];
  }

  ngOnInit(): void {
    forkJoin({
      accounts: this.api.getAccounts({ active: true }),
      types: this.lookupService.getLookup('transaction-types')
    }).subscribe({
      next: ({ accounts, types }) => {
        this.accounts = accounts || [];
        this.transactionTypes = (types || []).map((t) => t.code);
        if (!this.transactionTypes.length) {
          this.transactionTypes = ['JOURNAL_VOUCHER', 'PAYMENT_VOUCHER', 'RECEIPT_VOUCHER'];
        }
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
    if (event.actionId === 'approve') {
      this.api.approveTransaction(id, this.actorEmail).subscribe({ next: () => { this.showSuccess('COMMON.APPROVE_SUCCESS'); this.load(); } });
      return;
    }
    if (event.actionId === 'cancel') {
      this.api.cancelTransaction(id, this.actorEmail).subscribe({ next: () => { this.showSuccess('COMMON.CANCEL_SUCCESS'); this.load(); } });
      return;
    }
    super.onTableAction(event);
  }

  protected fetchList(filters: Record<string, string>): Observable<AccountingTransactionDto[]> {
    return this.api.getTransactions(filters);
  }

  protected fetchOne(id: number): Observable<AccountingTransactionDto> {
    return this.api.getTransaction(id);
  }

  protected createItem(payload: AccountingTransactionForm): Observable<AccountingTransactionDto> {
    return this.api.createTransaction(payload);
  }

  protected updateItem(id: number, payload: AccountingTransactionForm): Observable<AccountingTransactionDto> {
    return this.api.updateTransaction(id, payload);
  }

  protected removeItem(_id: number): Observable<void> {
    return of(undefined);
  }

  protected defaultFormValues(): Record<string, unknown> {
    return {
      transactionDate: new Date().toISOString().slice(0, 10),
      reference: '',
      description: '',
      transactionType: 'JOURNAL_VOUCHER',
      amount: 0,
      debitAccountId: null,
      creditAccountId: null,
      relatedDocumentReference: ''
    };
  }

  protected patchForm(dto: AccountingTransactionDto): void {
    this.form.reset({
      transactionDate: dto.transactionDate || '',
      reference: dto.reference || '',
      description: dto.description || '',
      transactionType: dto.transactionType || 'JOURNAL_VOUCHER',
      amount: Number(dto.amount || 0),
      debitAccountId: null,
      creditAccountId: null,
      relatedDocumentReference: ''
    });
  }

  protected toPayload(): AccountingTransactionForm {
    const v = this.form.getRawValue();
    return {
      transactionDate: v.transactionDate!,
      reference: v.reference || undefined,
      description: v.description || undefined,
      transactionType: v.transactionType!,
      amount: Number(v.amount),
      debitAccountId: Number(v.debitAccountId),
      creditAccountId: Number(v.creditAccountId),
      relatedDocumentReference: v.relatedDocumentReference || undefined
    };
  }

  protected mapRow(dto: AccountingTransactionDto): Record<string, unknown> {
    return {
      ...dto,
      amount: Number(dto.amount || 0).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })
    };
  }
}
