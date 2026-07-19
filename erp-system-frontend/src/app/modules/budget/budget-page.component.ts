import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Observable, throwError } from 'rxjs';
import { AccountDto, BudgetDto, BudgetForm } from '../../core/models/accounting.models';
import { AuthService } from '../../core/auth/auth.service';
import { AccountingApiService } from '../../core/services/accounting-api.service';
import { ConfirmDialogService } from '../../core/services/confirm-dialog.service';
import { DataTableAction, DataTableColumn } from '../../shared/components/data-table/data-table.component';
import { ErpMasterPageBase, MasterPageConfig, MASTER_CRUD_ACTIONS } from '../../shared/utils/erp-master-page.base';

@Component({
  standalone: false,
  selector: 'app-budget-page',
  templateUrl: './budget-page.component.html',
  styleUrls: ['./budget-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class BudgetPageComponent extends ErpMasterPageBase<BudgetDto, BudgetForm> implements OnInit, OnDestroy {
  readonly config: MasterPageConfig = {
    titleKey: 'BUDGET.TITLE',
    createKey: 'BUDGET.CREATE',
    editKey: 'COMMON.EDIT',
    viewKey: 'COMMON.VIEW',
    showStatus: true,
    statusOptions: ['DRAFT', 'APPROVED', 'ACTIVE', 'CLOSED']
  };

  readonly columns: DataTableColumn[] = [
    { key: 'budgetName', title: 'BUDGET.NAME', align: 'start' },
    { key: 'accountCode', title: 'JOURNAL.ACCOUNT_CODE', align: 'start' },
    { key: 'budgetYear', title: 'BUDGET.YEAR' },
    { key: 'budgetMonth', title: 'BUDGET.MONTH' },
    { key: 'plannedAmount', title: 'BUDGET.PLANNED', align: 'end' },
    { key: 'actualAmount', title: 'BUDGET.ACTUAL', align: 'end' },
    { key: 'variance', title: 'BUDGET.VARIANCE', align: 'end' },
    { key: 'status', title: 'COMMON.STATUS', kind: 'status' }
  ];

  readonly form = this.fb.group({
    accountId: [null as number | null, Validators.required],
    budgetName: [''],
    budgetYear: [new Date().getFullYear(), [Validators.required, Validators.min(2000)]],
    budgetMonth: [null as number | null],
    plannedAmount: [0, [Validators.required, Validators.min(0)]],
    status: ['DRAFT', Validators.required],
    notes: ['']
  });

  accounts: AccountDto[] = [];
  budgetStatuses = ['DRAFT', 'APPROVED', 'ACTIVE', 'CLOSED'];
  months = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12];
  editingRecord: BudgetDto | null = null;

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
      { id: 'activate', labelKey: 'BUDGET.ACTIVATE', className: 'erp-action-success', disabledWhen: (r) => String(r['status']) === 'ACTIVE' || String(r['status']) === 'CLOSED' },
      { id: 'close', labelKey: 'BUDGET.CLOSE', className: 'erp-action-warning', disabledWhen: (r) => String(r['status']) === 'CLOSED' }
    ];
  }

  get accountOptions() {
    return [{ id: null, label: '—' }, ...(this.accounts || []).map((a) => ({ id: a.id, label: `${a.code} - ${a.nameEn || a.name}` }))];
  }

  get monthOptions() {
    return [{ id: null, label: '—' }, ...this.months.map((m) => ({ id: m, label: String(m) }))];
  }

  ngOnInit(): void {
    this.api.getAccounts({ active: true }).subscribe({
      next: (accounts) => {
        this.accounts = accounts || [];
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
    if (event.actionId === 'activate') {
      this.api.changeBudgetStatus(id, 'ACTIVE').subscribe({ next: () => { this.showSuccess('BUDGET.ACTIVATE_SUCCESS'); this.load(); } });
      return;
    }
    if (event.actionId === 'close') {
      this.api.changeBudgetStatus(id, 'CLOSED').subscribe({ next: () => { this.showSuccess('BUDGET.CLOSE_SUCCESS'); this.load(); } });
      return;
    }
    super.onTableAction(event);
  }

  protected fetchList(filters: Record<string, string>): Observable<BudgetDto[]> {
    return this.api.getBudgets(filters);
  }

  protected fetchOne(id: number): Observable<BudgetDto> {
    return this.api.getBudget(id);
  }

  protected createItem(payload: BudgetForm): Observable<BudgetDto> {
    return this.api.createBudget(payload);
  }

  protected updateItem(id: number, payload: BudgetForm): Observable<BudgetDto> {
    return this.api.updateBudget(id, payload);
  }

  protected removeItem(_id: number): Observable<void> {
    return throwError(() => new Error('Budgets cannot be deleted; use Activate/Close instead.'));
  }

  protected defaultFormValues(): Record<string, unknown> {
    return {
      accountId: null,
      budgetName: '',
      budgetYear: new Date().getFullYear(),
      budgetMonth: null,
      plannedAmount: 0,
      status: 'DRAFT',
      notes: ''
    };
  }

  protected patchForm(dto: BudgetDto): void {
    this.editingRecord = dto;
    this.form.reset({
      accountId: dto.accountId || null,
      budgetName: dto.budgetName || '',
      budgetYear: dto.budgetYear,
      budgetMonth: dto.budgetMonth || null,
      plannedAmount: Number(dto.plannedAmount || 0),
      status: dto.status || 'DRAFT',
      notes: dto.notes || ''
    });
  }

  protected toPayload(): BudgetForm {
    const v = this.form.getRawValue();
    return {
      accountId: Number(v.accountId),
      budgetName: v.budgetName || undefined,
      budgetYear: Number(v.budgetYear),
      budgetMonth: v.budgetMonth ? Number(v.budgetMonth) : null,
      plannedAmount: Number(v.plannedAmount),
      status: v.status!,
      notes: v.notes || undefined
    };
  }

  protected mapRow(dto: BudgetDto): Record<string, unknown> {
    return {
      ...dto,
      plannedAmount: Number(dto.plannedAmount || 0).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 }),
      actualAmount: Number(dto.actualAmount || 0).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 }),
      variance: Number(dto.variance || 0).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 }),
      budgetMonth: dto.budgetMonth || '—'
    };
  }
}
