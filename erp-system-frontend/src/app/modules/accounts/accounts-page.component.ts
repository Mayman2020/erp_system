import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { finalize } from 'rxjs/operators';
import { AccountDto, AccountFormDto, AccountTreeDto, AccountingType } from '../../core/models/accounting.models';
import { AccountingApiService } from '../../core/services/accounting-api.service';
import { LookupItem } from '../../core/models/lookup.models';
import { LookupService } from '../../core/services/lookup.service';
import { TranslationService } from '../../core/i18n/translation.service';
import { ConfirmDialogService } from '../../core/services/confirm-dialog.service';

@Component({
  standalone: false,
  selector: 'app-accounts-page',
  templateUrl: './accounts-page.component.html',
  styleUrls: ['./accounts-page.component.scss']
})
export class AccountsPageComponent implements OnInit {
  loading = false;
  actionLoading = false;
  errorKey = '';
  successKey = '';

  accountTypeOptions: LookupItem[] = [];
  statusOptions: LookupItem[] = [];
  rows: AccountDto[] = [];
  pagedRows: AccountDto[] = [];
  treeRows: AccountTreeDto[] = [];

  query = '';
  selectedType: AccountingType | '' = '';
  selectedActive: 'ALL' | 'ACTIVE' | 'INACTIVE' = 'ALL';
  selectedFinancialStatement: '' | 'BALANCE_SHEET' | 'INCOME_STATEMENT' = '';
  sortBy: 'code' | 'name' | 'accountType' = 'code';
  sortDir: 'asc' | 'desc' = 'asc';

  viewMode: 'table' | 'tree' = 'table';
  dialogVisible = false;
  dialogMode: 'create' | 'edit' | 'view' = 'create';
  selectedAccountId: number | null = null;

  readonly form = this.fb.group({
    code: [''],
    nameEn: ['', Validators.required],
    nameAr: [''],
    accountType: ['ASSET', Validators.required],
    parentId: [null as number | null],
    statusCode: ['ACTIVE'],
    openingBalance: [0],
    openingBalanceSide: ['DEBIT']
  });

  readonly columns: Array<{ key: string; title: string; kind?: 'text' | 'status' | 'boolean' | 'type'; prefix?: string; clickable?: boolean }> = [
    { key: 'code', title: 'ACCOUNTS.CODE', kind: 'text' },
    { key: 'name', title: 'COMMON.NAME', kind: 'text', clickable: true },
    { key: 'accountType', title: 'ACCOUNTS.TYPE', kind: 'type', prefix: 'ACCOUNT_TYPE.' },
    { key: 'financialStatement', title: 'ACCOUNTS.FINANCIAL_STATEMENT', kind: 'type', prefix: 'FINANCIAL_STATEMENT.' },
    { key: 'status', title: 'COMMON.STATUS', kind: 'status', prefix: 'STATUS.' }
  ];

  readonly rowActions = [
    { id: 'view', labelKey: 'COMMON.VIEW', className: 'btn-outline-info' },
    { id: 'edit', labelKey: 'COMMON.EDIT', className: 'btn-outline-primary' },
    { id: 'toggle', labelKey: 'COMMON.TOGGLE_STATUS', className: 'btn-outline-warning' }
  ];

  constructor(
    private api: AccountingApiService,
    private fb: FormBuilder,
    private translationService: TranslationService,
    private confirmDialog: ConfirmDialogService,
    private lookupService: LookupService,
    private cdr: ChangeDetectorRef
  ) {
    this.form.get('accountType')?.valueChanges.subscribe((type) => {
      const side = this.defaultBalanceSide(type as AccountingType);
      this.form.patchValue({ openingBalanceSide: side }, { emitEvent: false });
    });
  }

  ngOnInit(): void {
    this.loadLookupsAndData();
  }

  onSearch(filters: { query?: string }): void {
    this.query = (filters && filters.query) || '';
    this.loadAccounts();
  }

  onTypeFilterChange(value: AccountingType | ''): void {
    this.selectedType = value;
    this.loadAccounts();
  }

  onActiveFilterChange(value: 'ALL' | 'ACTIVE' | 'INACTIVE'): void {
    this.selectedActive = value;
    this.loadAccounts();
  }

  onFinancialStatementFilterChange(value: '' | 'BALANCE_SHEET' | 'INCOME_STATEMENT'): void {
    this.selectedFinancialStatement = value;
    this.applyTableState();
  }

  setSort(field: 'code' | 'name' | 'accountType', direction: 'asc' | 'desc'): void {
    this.sortBy = field;
    this.sortDir = direction;
    this.applyTableState();
  }

  openCreateDialog(): void {
    this.dialogMode = 'create';
    this.selectedAccountId = null;
    this.form.reset({
      code: '',
      nameEn: '',
      nameAr: '',
      accountType: 'ASSET',
      parentId: null,
      statusCode: 'ACTIVE',
      openingBalance: 0,
      openingBalanceSide: 'DEBIT'
    });
    this.form.enable();
    this.dialogVisible = true;
  }

  openViewDialog(account: AccountDto): void {
    this.dialogMode = 'view';
    this.selectedAccountId = account.id;
    this.setFormFromAccount(account);
    this.form.disable();
    this.dialogVisible = true;
  }

  openEditDialog(account: AccountDto): void {
    this.dialogMode = 'edit';
    this.selectedAccountId = account.id;
    this.setFormFromAccount(account);
    this.form.enable();
    this.dialogVisible = true;
  }

  closeDialog(): void {
    this.dialogVisible = false;
  }

  toggleTreeView(): void {
    this.viewMode = this.viewMode === 'table' ? 'tree' : 'table';
    if (this.viewMode === 'tree' && !this.treeRows.length) {
      this.loadAccountTree();
    }
  }

  saveAccount(): void {
    if (this.form.invalid || this.dialogMode === 'view') {
      this.form.markAllAsTouched();
      return;
    }

    const acctType = this.form.value.accountType as AccountingType;
    const payload: AccountFormDto = {
      code: this.form.value.code || undefined,
      name: this.form.value.nameAr || this.form.value.nameEn || '',
      nameEn: this.form.value.nameEn || '',
      nameAr: this.form.value.nameAr || undefined,
      parentId: this.form.value.parentId,
      accountType: acctType,
      active: this.form.value.statusCode === 'ACTIVE',
      openingBalance: Number(this.form.value.openingBalance || 0),
      openingBalanceSide: (this.form.value.openingBalanceSide as 'DEBIT' | 'CREDIT') || this.defaultBalanceSide(acctType)
    };

    this.actionLoading = true;
    this.errorKey = '';
    this.successKey = '';

    const request$ =
      this.dialogMode === 'edit' && this.selectedAccountId
        ? this.api.updateAccount(this.selectedAccountId, payload)
        : this.api.createAccount(payload);

    request$
      .pipe(finalize(() => (this.actionLoading = false)))
      .subscribe({
        next: () => {
          this.successKey = 'ACCOUNTS.SAVE_SUCCESS';
          this.dialogVisible = false;
          this.loadAll();
        },
        error: (err) => {
          this.errorKey = this.extractError(err, 'ACCOUNTS.SAVE_ERROR');
          console.error('Account save failed:', err);
        }
      });
  }

  toggleActive(account: AccountDto): void {
    const key = account.active ? 'ACCOUNTS.CONFIRM_DEACTIVATE' : 'ACCOUNTS.CONFIRM_ACTIVATE';
    this.confirmDialog.confirmByKey({ messageKey: key }).subscribe((ok) => {
      if (!ok) {
        return;
      }

      this.actionLoading = true;
      this.errorKey = '';
      this.successKey = '';
      const request$ = account.active ? this.api.deactivateAccount(account.id) : this.api.activateAccount(account.id);
      request$
        .pipe(finalize(() => (this.actionLoading = false)))
        .subscribe({
          next: () => {
            this.successKey = account.active ? 'ACCOUNTS.DEACTIVATE_SUCCESS' : 'ACCOUNTS.ACTIVATE_SUCCESS';
            this.loadAll();
          },
          error: () => {
            this.errorKey = account.active ? 'ACCOUNTS.DEACTIVATE_ERROR' : 'ACCOUNTS.ACTIVATE_ERROR';
          }
        });
    });
  }

  onTableAction(event: { actionId: string; row: Record<string, unknown> }): void {
    const account = this.rows.find((item) => item.id === Number(event.row.id));
    if (!account) {
      return;
    }
    if (event.actionId === 'view') {
      this.openViewDialog(account);
      return;
    }
    if (event.actionId === 'edit') {
      this.openEditDialog(account);
      return;
    }
    this.toggleActive(account);
  }

  private loadAll(): void {
    this.loadAccounts();
    this.loadAccountTree();
  }

  private loadLookupsAndData(): void {
    forkJoin({
      accountTypes: this.lookupService.getLookup('account-types'),
      statuses: this.lookupService.getLookup('status')
    }).subscribe({
      next: (lookup) => {
        this.accountTypeOptions = lookup.accountTypes;
        this.statusOptions = lookup.statuses;
        this.loadAll();
      },
      error: () => {
        this.accountTypeOptions = [];
        this.statusOptions = [];
        this.loadAll();
      }
    });
  }

  private loadAccounts(): void {
    this.loading = true;
    this.errorKey = '';
    this.api
      .getAccounts({
        search: this.query || undefined,
        type: this.selectedType || undefined,
        active: this.selectedActive === 'ALL' ? '' : this.selectedActive === 'ACTIVE'
      })
      .pipe(
        finalize(() => {
          this.loading = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: (rows) => {
          this.rows = rows || [];
          this.applyTableState();
        },
        error: () => {
          this.errorKey = 'COMMON.ERROR_LOADING';
        }
      });
  }

  private loadAccountTree(): void {
    this.api.getAccountTree().subscribe({
      next: (tree) => {
        this.treeRows = tree || [];
      },
      error: () => {
        this.treeRows = [];
      }
    });
  }

  private applyTableState(): void {
    const filtered = this.selectedFinancialStatement
      ? this.rows.filter((row) => row.financialStatement === this.selectedFinancialStatement)
      : this.rows;
    const sorted = [...filtered].sort((a, b) => {
      const aValue = (a[this.sortBy] || '').toString().toLowerCase();
      const bValue = (b[this.sortBy] || '').toString().toLowerCase();
      const result = aValue > bValue ? 1 : aValue < bValue ? -1 : 0;
      return this.sortDir === 'asc' ? result : -result;
    });

    this.pagedRows = sorted.map((row) => ({
      ...row,
      status: row.active ? 'ACTIVE' : 'INACTIVE'
    }));
  }

  private setFormFromAccount(account: AccountDto): void {
    this.form.patchValue({
      code: account.code,
      nameEn: account.nameEn || account.name,
      nameAr: account.nameAr || '',
      accountType: account.accountType,
      parentId: account.parentId,
      statusCode: account.active ? 'ACTIVE' : 'INACTIVE',
      openingBalance: account.openingBalance || 0,
      openingBalanceSide: account.openingBalanceSide || 'DEBIT'
    });
  }

  private defaultBalanceSide(type: AccountingType | string): 'DEBIT' | 'CREDIT' {
    return type === 'ASSET' || type === 'EXPENSE' ? 'DEBIT' : 'CREDIT';
  }

  private extractError(err: any, fallback: string): string {
    const serverMsg = err?.error?.message;
    return serverMsg && serverMsg !== 'COMMON.OK' ? serverMsg : fallback;
  }
}
