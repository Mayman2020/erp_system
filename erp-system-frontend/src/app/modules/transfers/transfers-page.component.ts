import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { forkJoin, Observable } from 'rxjs';
import { AccountDto, TransferDto, TransferForm } from '../../core/models/accounting.models';
import { AuthService } from '../../core/auth/auth.service';
import { AccountingApiService } from '../../core/services/accounting-api.service';
import { ConfirmDialogService } from '../../core/services/confirm-dialog.service';
import { DataTableAction, DataTableColumn } from '../../shared/components/data-table/data-table.component';
import { ErpMasterPageBase, MasterPageConfig, MASTER_CRUD_ACTIONS } from '../../shared/utils/erp-master-page.base';

@Component({
  standalone: false,
  selector: 'app-transfers-page',
  templateUrl: './transfers-page.component.html',
  styleUrls: ['./transfers-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TransfersPageComponent extends ErpMasterPageBase<TransferDto, TransferForm> implements OnInit, OnDestroy {
  readonly config: MasterPageConfig = {
    titleKey: 'TRANSFERS.TITLE',
    createKey: 'TRANSFERS.CREATE',
    editKey: 'COMMON.EDIT',
    viewKey: 'COMMON.VIEW',
    showStatus: true,
    statusOptions: ['DRAFT', 'POSTED', 'CANCELLED']
  };

  readonly columns: DataTableColumn[] = [
    { key: 'reference', title: 'TRANSFERS.REFERENCE' },
    { key: 'transferDate', title: 'COMMON.DATE', kind: 'date' },
    { key: 'amount', title: 'ERP.AMOUNT', align: 'end' },
    { key: 'sourceAccountName', title: 'TRANSFERS.SOURCE' },
    { key: 'destinationAccountName', title: 'TRANSFERS.DESTINATION' },
    { key: 'status', title: 'COMMON.STATUS', kind: 'status' }
  ];

  readonly form = this.fb.group({
    transferDate: [new Date().toISOString().slice(0, 10), Validators.required],
    reference: [''],
    description: [''],
    amount: [0, [Validators.required, Validators.min(0.01)]],
    sourceAccountId: [null as number | null, Validators.required],
    destinationAccountId: [null as number | null, Validators.required]
  });

  accounts: AccountDto[] = [];
  currentJournalEntryId?: number;

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
      { id: 'post', labelKey: 'TRANSFERS.POST', className: 'erp-action-success', disabledWhen: (r) => String(r['status']) !== 'DRAFT' },
      { id: 'cancel', labelKey: 'COMMON.CANCEL', className: 'erp-action-warning', disabledWhen: (r) => String(r['status']) !== 'POSTED' },
      MASTER_CRUD_ACTIONS[2]
    ];
  }

  get accountOptions() {
    return [{ id: null, label: '—' }, ...(this.accounts || []).map((a) => ({ id: a.id, label: `${a.code} - ${a.nameEn || a.name}` }))];
  }

  ngOnInit(): void {
    forkJoin({ accounts: this.api.getAccounts({ active: true }) }).subscribe({
      next: ({ accounts }) => {
        this.accounts = accounts || [];
        this.initMasterPage();
      },
      error: () => this.initMasterPage()
    });
  }

  ngOnDestroy(): void {
    this.destroyMasterPage();
  }

  override onTableAction(event: { actionId: string; row: Record<string, unknown> }): void {
    const id = Number(event.row['id']);
    if (!id) return;
    if (event.actionId === 'post') {
      this.api.postTransfer(id, this.actorEmail).subscribe({ next: () => this.load() });
      return;
    }
    if (event.actionId === 'cancel') {
      this.api.cancelTransfer(id, this.actorEmail).subscribe({ next: () => this.load() });
      return;
    }
    super.onTableAction(event);
  }

  protected fetchList(_filters?: Record<string, string>): Observable<TransferDto[]> {
    return this.api.getTransfers();
  }

  protected fetchOne(id: number): Observable<TransferDto> {
    return this.api.getTransfer(id);
  }

  protected createItem(payload: TransferForm): Observable<TransferDto> {
    return this.api.createTransfer(payload);
  }

  protected updateItem(id: number, payload: TransferForm): Observable<TransferDto> {
    return this.api.updateTransfer(id, payload);
  }

  protected removeItem(id: number): Observable<void> {
    return this.api.deleteTransfer(id);
  }

  protected defaultFormValues(): Record<string, unknown> {
    return { transferDate: new Date().toISOString().slice(0, 10), reference: '', description: '', amount: 0, sourceAccountId: null, destinationAccountId: null };
  }

  protected patchForm(dto: TransferDto): void {
    this.currentJournalEntryId = dto.journalEntryId;
    this.form.patchValue({
      transferDate: dto.transferDate,
      reference: dto.reference || '',
      description: dto.description || '',
      amount: dto.amount,
      sourceAccountId: dto.sourceAccountId,
      destinationAccountId: dto.destinationAccountId
    });
  }

  protected toPayload(): TransferForm {
    const v = this.form.getRawValue();
    return {
      transferDate: v.transferDate!,
      reference: v.reference || undefined,
      description: v.description || undefined,
      amount: Number(v.amount),
      sourceAccountId: Number(v.sourceAccountId),
      destinationAccountId: Number(v.destinationAccountId)
    };
  }

  protected mapRow(dto: TransferDto): Record<string, unknown> {
    return {
      ...dto,
      amount: Number(dto.amount).toLocaleString(undefined, { minimumFractionDigits: 2 }),
      sourceAccountName: dto.sourceAccountName || dto.sourceAccountCode,
      destinationAccountName: dto.destinationAccountName || dto.destinationAccountCode
    };
  }
}
