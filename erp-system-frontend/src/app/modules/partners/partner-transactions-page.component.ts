import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { forkJoin, Observable } from 'rxjs';
import { PartnerDto, PartnerTransactionDto, PartnerTransactionForm } from '../../core/models/partners.models';
import { AuthService } from '../../core/auth/auth.service';
import { PartnersApiService } from '../../core/services/partners-api.service';
import { ConfirmDialogService } from '../../core/services/confirm-dialog.service';
import { DataTableAction, DataTableColumn } from '../../shared/components/data-table/data-table.component';
import { ErpMasterPageBase, MasterPageConfig, MASTER_CRUD_ACTIONS } from '../../shared/utils/erp-master-page.base';

@Component({
  standalone: false,
  selector: 'app-partner-transactions-page',
  templateUrl: './partner-transactions-page.component.html',
  styleUrls: ['./partner-transactions-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PartnerTransactionsPageComponent extends ErpMasterPageBase<PartnerTransactionDto, PartnerTransactionForm> implements OnInit, OnDestroy {
  readonly config: MasterPageConfig = {
    titleKey: 'PARTNERS.TRANSACTIONS_TITLE',
    createKey: 'PARTNERS.TRANSACTIONS_CREATE',
    editKey: 'COMMON.EDIT',
    viewKey: 'COMMON.VIEW',
    showStatus: true,
    statusOptions: ['DRAFT', 'APPROVED', 'CANCELLED']
  };

  readonly columns: DataTableColumn[] = [
    { key: 'partnerName', title: 'PARTNERS.TITLE' },
    { key: 'txnType', title: 'PARTNERS.TXN_TYPE' },
    { key: 'txnDate', title: 'COMMON.DATE', kind: 'date' },
    { key: 'amount', title: 'ERP.AMOUNT', align: 'end' },
    { key: 'status', title: 'COMMON.STATUS', kind: 'status' }
  ];

  readonly txnTypeOptions = [
    { id: 'CAPITAL', labelKey: 'PARTNERS.TXN_CAPITAL' },
    { id: 'DRAWING', labelKey: 'PARTNERS.TXN_DRAWING' }
  ];

  readonly form = this.fb.group({
    partnerId: [null as number | null, Validators.required],
    txnType: ['CAPITAL', Validators.required],
    amount: [0, [Validators.required, Validators.min(0.01)]],
    txnDate: [new Date().toISOString().slice(0, 10), Validators.required],
    notes: ['']
  });

  partners: PartnerDto[] = [];
  currentJournalEntryId?: number;

  constructor(
    private api: PartnersApiService,
    private fb: FormBuilder,
    authService: AuthService,
    confirmDialog: ConfirmDialogService,
    cdr: ChangeDetectorRef
  ) {
    super(authService, confirmDialog, cdr);
  }

  get partnerLovItems() {
    return (this.partners || []).map((p) => ({ id: p.id, label: `${p.code} - ${p.name}` }));
  }

  get partnerOptions() {
    return [{ id: null, label: '—' }, ...this.partnerLovItems];
  }

  get tableActions(): DataTableAction[] {
    return [
      ...MASTER_CRUD_ACTIONS.slice(0, 2),
      { id: 'approve', labelKey: 'COMMON.APPROVE', className: 'erp-action-success', disabledWhen: (r) => String(r['status']) !== 'DRAFT' },
      MASTER_CRUD_ACTIONS[2]
    ];
  }

  ngOnInit(): void {
    this.api.getPartners().subscribe({
      next: (partners) => {
        this.partners = partners || [];
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
    if (event.actionId === 'approve') {
      this.api.approveTransaction(id, this.actorEmail).subscribe({ next: () => this.load() });
      return;
    }
    super.onTableAction(event);
  }

  protected fetchList(_filters?: Record<string, string>): Observable<PartnerTransactionDto[]> {
    return this.api.getTransactions();
  }

  protected fetchOne(id: number): Observable<PartnerTransactionDto> {
    return this.api.getTransaction(id);
  }

  protected createItem(payload: PartnerTransactionForm): Observable<PartnerTransactionDto> {
    return this.api.createTransaction(payload);
  }

  protected updateItem(id: number, payload: PartnerTransactionForm): Observable<PartnerTransactionDto> {
    return this.api.updateTransaction(id, payload);
  }

  protected removeItem(id: number): Observable<void> {
    return this.api.deleteTransaction(id);
  }

  protected defaultFormValues(): Record<string, unknown> {
    return { partnerId: null, txnType: 'CAPITAL', amount: 0, txnDate: new Date().toISOString().slice(0, 10), notes: '' };
  }

  protected patchForm(dto: PartnerTransactionDto): void {
    this.currentJournalEntryId = dto.journalEntryId;
    this.form.patchValue({
      partnerId: dto.partnerId,
      txnType: dto.txnType,
      amount: dto.amount,
      txnDate: dto.txnDate,
      notes: dto.notes || ''
    });
  }

  protected toPayload(): PartnerTransactionForm {
    const v = this.form.getRawValue();
    return {
      partnerId: Number(v.partnerId),
      txnType: String(v.txnType),
      amount: Number(v.amount),
      txnDate: String(v.txnDate),
      notes: v.notes || undefined
    };
  }

  protected mapRow(dto: PartnerTransactionDto): Record<string, unknown> {
    return {
      ...dto,
      partnerName: dto.partnerCode ? `${dto.partnerCode} - ${dto.partnerName}` : dto.partnerName
    };
  }
}
