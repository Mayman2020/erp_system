import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { finalize } from 'rxjs/operators';
import { AccountTreeDto, JournalEntry, JournalEntryForm, JournalEntryLine } from '../../core/models/accounting.models';
import { LookupItem } from '../../core/models/lookup.models';
import { TranslationService } from '../../core/i18n/translation.service';
import { AuthService } from '../../core/auth/auth.service';
import { AccountingApiService } from '../../core/services/accounting-api.service';
import { ConfirmDialogService } from '../../core/services/confirm-dialog.service';
import { LookupService } from '../../core/services/lookup.service';

@Component({ standalone: false,
  selector: 'app-journal-entry-page',
  templateUrl: './journal-entry-page.component.html',
  styleUrls: ['./journal-entry-page.component.scss']
})
export class JournalEntryPageComponent implements OnInit, OnDestroy {
  loading = false;
  saving = false;
  errorKey = '';
  successKey = '';

  entries: JournalEntry[] = [];
  filteredEntries: Array<Record<string, unknown>> = [];

  accountTree: AccountTreeDto[] = [];
  statusLookups: LookupItem[] = [];
  statusCodes: string[] = [];
  currencyLookups: LookupItem[] = [];
  entryTypeLookups: LookupItem[] = [];

  filterStatus = '';
  filterMinAmount: number | null = null;
  filterMaxAmount: number | null = null;
  filterEntryNumber = '';

  formVisible = false;
  accountPickerVisible = false;
  formMode: 'create' | 'edit' | 'view' = 'create';
  selectedEntry: JournalEntry | null = null;
  selectedLineIndex = 0;
  actorEmail = 'system@erp.local';
  private feedbackTimer: ReturnType<typeof setTimeout> | null = null;

  readonly columns = [
    { key: 'referenceNumber', title: 'JOURNAL.ENTRY_NUMBER', kind: 'text' as 'text' },
    { key: 'description', title: 'JOURNAL.DESCRIPTION', kind: 'text' as 'text', align: 'start' as 'start' },
    { key: 'status', title: 'COMMON.STATUS', kind: 'status' as 'status', prefix: 'STATUS.' },
    { key: 'totalDebit', title: 'JOURNAL.TOTAL_DEBIT', kind: 'text' as 'text', align: 'end' as 'end' },
    { key: 'totalCredit', title: 'JOURNAL.TOTAL_CREDIT', kind: 'text' as 'text', align: 'end' as 'end' },
    { key: 'createdAt', title: 'JOURNAL.CREATED_AT', kind: 'date' as 'date' },
    { key: 'createdBy', title: 'JOURNAL.CREATED_BY', kind: 'text' as 'text', align: 'start' as 'start' }
  ];

  readonly actions = [
    { id: 'view', labelKey: 'COMMON.VIEW', className: 'erp-action-secondary' },
    {
      id: 'edit',
      labelKey: 'COMMON.EDIT',
      className: 'erp-action-info',
      disabledWhen: (row: Record<string, unknown>) => String(row['status'] || '') !== 'DRAFT'
    },
    {
      id: 'approve',
      labelKey: 'COMMON.APPROVE',
      className: 'erp-action-success',
      disabledWhen: (row: Record<string, unknown>) => String(row['status'] || '') !== 'DRAFT'
    },
    {
      id: 'reverse',
      labelKey: 'JOURNAL.REVERSE',
      className: 'erp-action-warning',
      disabledWhen: (row: Record<string, unknown>) => String(row['status'] || '') !== 'APPROVED'
    }
  ];

  readonly form: FormGroup = this.fb.group({
    entryDate: ['', Validators.required],
    description: [''],
    externalReference: [''],
    currencyCode: ['', Validators.required],
    entryType: ['', Validators.required],
    lines: this.fb.array([])
  });

  constructor(
    private api: AccountingApiService,
    private lookupService: LookupService,
    private translationService: TranslationService,
    private confirmDialog: ConfirmDialogService,
    private authService: AuthService,
    private fb: FormBuilder,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.authService.currentUser$.subscribe((user) => {
      this.actorEmail = user?.email || user?.username || 'system@erp.local';
    });
    this.authService.refreshCurrentUser();
    this.loadLookupsAndData();
  }

  ngOnDestroy(): void {
    if (this.feedbackTimer) {
      clearTimeout(this.feedbackTimer);
      this.feedbackTimer = null;
    }
  }

  get lines(): FormArray {
    return this.form.get('lines') as FormArray;
  }

  onSearch(filters: { query?: string; fromDate?: string; toDate?: string; status?: string; minAmount?: string; maxAmount?: string }): void {
    this.filterEntryNumber = filters.query || '';
    this.filterStatus = filters.status || '';
    this.filterMinAmount = filters.minAmount ? Number(filters.minAmount) : null;
    this.filterMaxAmount = filters.maxAmount ? Number(filters.maxAmount) : null;
    this.loadEntries({
      search: this.filterEntryNumber || undefined,
      fromDate: filters.fromDate || undefined,
      toDate: filters.toDate || undefined,
      status: this.filterStatus || undefined
    });
  }

  onStatusFilterChange(value: string): void {
    this.filterStatus = value || '';
    this.applyClientFilters();
  }

  onAmountFilterChange(): void {
    this.applyClientFilters();
  }

  openCreate(): void {
    this.formMode = 'create';
    this.selectedEntry = null;
    this.resetForm();
    this.formVisible = true;
  }

  onRowAction(event: { actionId: string; row: Record<string, unknown> }): void {
    const entry = this.entries.find((item) => item.id === Number(event.row.id));
    if (!entry) {
      return;
    }
    if (event.actionId === 'view') {
      this.openView(entry.id);
      return;
    }
    if (event.actionId === 'edit') {
      if (entry.status !== 'DRAFT') {
        this.showError('JOURNAL.EDIT_ONLY_DRAFT');
        return;
      }
      this.openEdit(entry.id);
      return;
    }
    if (event.actionId === 'approve') {
      this.approveEntry(entry);
      return;
    }
    if (event.actionId === 'reverse') {
      this.reverseEntry(entry);
    }
  }

  addLine(): void {
    this.lines.push(
      this.fb.group({
        accountId: [null, Validators.required],
        accountName: [''],
        description: [''],
        debit: [0],
        credit: [0]
      })
    );
  }

  removeLine(index: number): void {
    if (this.lines.length <= 2) {
      return;
    }
    this.lines.removeAt(index);
  }

  pickLine(index: number): void {
    this.selectedLineIndex = index;
  }

  selectAccount(account: AccountTreeDto): void {
    const line = this.lines.at(this.selectedLineIndex);
    if (!line) {
      return;
    }
    line.patchValue({
      accountId: account.id,
      accountName: `${account.code} - ${account.name}`
    });
    this.errorKey = '';
    this.accountPickerVisible = false;
  }

  openAccountSelector(index: number): void {
    this.pickLine(index);
    this.accountPickerVisible = true;
  }

  closeAccountSelector(): void {
    this.accountPickerVisible = false;
  }

  save(): void {
    const validationKey = this.getJournalValidationMessageKey();
    if (validationKey) {
      this.markJournalFormTouched();
      this.showError(validationKey);
      this.cdr.detectChanges();
      return;
    }
    this.saving = true;
    this.errorKey = '';
    const payload = this.toPayload();
    const request$ =
      this.formMode === 'edit' && this.selectedEntry ? this.api.updateJournalEntry(this.selectedEntry.id, payload) : this.api.createJournalEntry(payload);
    request$
      .pipe(
        finalize(() => {
          this.saving = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: () => {
          this.showSuccess('JOURNAL.SAVE_SUCCESS');
          this.formVisible = false;
          this.loadEntries();
        },
        error: (err) => {
          const msg = err?.error?.message;
          this.showError(msg && msg !== 'COMMON.OK' ? msg : 'JOURNAL.SAVE_ERROR');
        }
      });
  }

  closeForm(): void {
    this.formVisible = false;
  }

  approveCurrent(): void {
    if (!this.selectedEntry) {
      return;
    }
    this.approveEntry(this.selectedEntry);
  }

  totalDebit(): number {
    return this.lines.controls.reduce((sum, row) => sum + Number(row.get('debit')?.value || 0), 0);
  }

  totalCredit(): number {
    return this.lines.controls.reduce((sum, row) => sum + Number(row.get('credit')?.value || 0), 0);
  }

  difference(): number {
    return this.totalDebit() - this.totalCredit();
  }

  isBalanced(): boolean {
    return this.difference() === 0 && this.totalDebit() > 0;
  }

  lineInvalid(index: number): boolean {
    const row = this.lines.at(index);
    if (!row) {
      return true;
    }
    const debit = Number(row.get('debit')?.value || 0);
    const credit = Number(row.get('credit')?.value || 0);
    return !(debit > 0 || credit > 0) || (debit > 0 && credit > 0);
  }

  canApproveSelected(): boolean {
    return !!this.selectedEntry && this.selectedEntry.status === 'DRAFT' && this.selectedEntry.balanced;
  }

  private loadLookupsAndData(): void {
    forkJoin({
      statuses: this.lookupService.getLookup('journal-entry-statuses'),
      currencies: this.lookupService.getLookup('currencies'),
      entryTypes: this.lookupService.getLookup('entry-types'),
      accounts: this.api.getAccountTree()
    }).subscribe({
      next: (data) => {
        this.statusLookups = data.statuses;
        this.statusCodes = data.statuses.map((item) => item.code);
        this.currencyLookups = data.currencies;
        this.entryTypeLookups = data.entryTypes;
        this.accountTree = data.accounts;
        this.resetForm();
        this.loadEntries();
      },
      error: () => {
        this.showError('COMMON.ERROR_LOADING');
      }
    });
  }

  private loadEntries(filters: Record<string, string | number | boolean> = {}): void {
    this.loading = true;
    this.api
      .getJournalEntries(filters)
      .pipe(
        finalize(() => {
          this.loading = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: (rows) => {
          this.entries = rows;
          this.applyClientFilters();
        },
        error: () => {
          this.showError('COMMON.ERROR_LOADING');
        }
      });
  }

  private applyClientFilters(): void {
    this.filteredEntries = this.entries
      .filter((entry) => !this.filterStatus || entry.status === this.filterStatus)
      .filter((entry) => (this.filterMinAmount == null ? true : Number(entry.totalDebit) >= this.filterMinAmount))
      .filter((entry) => (this.filterMaxAmount == null ? true : Number(entry.totalDebit) <= this.filterMaxAmount))
      .map((entry) => ({
        id: entry.id,
        referenceNumber: entry.referenceNumber,
        entryDate: entry.entryDate,
        description: entry.description || '',
        totalDebit: Number(entry.totalDebit).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 }),
        totalCredit: Number(entry.totalCredit).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 }),
        status: entry.status,
        createdAt: entry.createdAt || '',
        createdBy: entry.createdBy || '',
        balanced: entry.balanced
      }));
  }

  private openView(id: number): void {
    this.formMode = 'view';
    this.openById(id);
  }

  private openEdit(id: number): void {
    this.formMode = 'edit';
    this.openById(id);
  }

  private openById(id: number): void {
    this.api
      .getJournalEntry(id)
      .pipe(
        finalize(() => {
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: (entry) => {
          this.selectedEntry = entry;
          this.patchForm(entry);
          if (this.formMode === 'view') {
            this.form.disable();
          } else {
            this.form.enable();
          }
          this.formVisible = true;
        },
        error: () => {
          this.showError('COMMON.ERROR_LOADING');
        }
      });
  }

  private patchForm(entry: JournalEntry): void {
    this.lines.clear();
    entry.lines.forEach((line) => {
      this.lines.push(
        this.fb.group({
          accountId: [line.accountId, Validators.required],
          accountName: [`${line.accountCode || ''} - ${this.accountName(line)}`],
          description: [line.description || ''],
          debit: [Number(line.debit || 0)],
          credit: [Number(line.credit || 0)]
        })
      );
    });
    this.form.patchValue({
      entryDate: entry.entryDate,
      description: entry.description || '',
      externalReference: entry.externalReference || '',
      currencyCode: entry.currencyCode || (this.currencyLookups[0]?.code || 'USD'),
      entryType: entry.entryType || (this.entryTypeLookups[0]?.code || 'MANUAL')
    });
  }

  private resetForm(): void {
    this.lines.clear();
    this.addLine();
    this.addLine();
    this.form.patchValue({
      entryDate: new Date().toISOString().substring(0, 10),
      description: '',
      externalReference: '',
      currencyCode: this.currencyLookups[0]?.code || 'USD',
      entryType: this.entryTypeLookups[0]?.code || 'MANUAL'
    });
    this.form.enable();
  }

  /** First blocking validation issue for user-facing messages (translation key). */
  private getJournalValidationMessageKey(): string | null {
    if (this.lines.length < 2) {
      return 'JOURNAL.VALIDATION_MIN_LINES';
    }
    for (const row of this.lines.controls) {
      const accountId = Number(row.get('accountId')?.value || 0);
      const debit = Number(row.get('debit')?.value || 0);
      const credit = Number(row.get('credit')?.value || 0);
      const hasOneSide = (debit > 0 && credit === 0) || (credit > 0 && debit === 0);
      if (accountId <= 0) {
        return 'JOURNAL.VALIDATION_LINE_ACCOUNT';
      }
      if (!hasOneSide) {
        return 'JOURNAL.VALIDATION_LINE_AMOUNT';
      }
    }
    if (!this.isBalanced()) {
      return 'JOURNAL.NOT_BALANCED';
    }
    if (this.form.invalid) {
      return 'JOURNAL.VALIDATION_HEADER';
    }
    return null;
  }

  private markJournalFormTouched(): void {
    this.form.markAllAsTouched();
    this.lines.controls.forEach((line) => {
      Object.values((line as FormGroup).controls).forEach((c) => c.markAsTouched());
    });
  }

  private toPayload(): JournalEntryForm {
    return {
      entryDate: this.form.value.entryDate,
      description: this.form.value.description,
      externalReference: this.form.value.externalReference,
      currencyCode: this.form.value.currencyCode,
      entryType: this.form.value.entryType,
      lines: this.lines.controls.map((row) => ({
        accountId: Number(row.get('accountId')?.value),
        description: row.get('description')?.value,
        debit: Number(row.get('debit')?.value || 0),
        credit: Number(row.get('credit')?.value || 0)
      }))
    };
  }

  private approveEntry(entry: JournalEntry): void {
    if (entry.status !== 'DRAFT') {
      this.showError('JOURNAL.APPROVE_ONLY_DRAFT');
      return;
    }
    this.confirmDialog.confirmByKey({ messageKey: 'JOURNAL.CONFIRM_APPROVE' }).subscribe((ok) => {
      if (ok !== true) {
        return;
      }
      this.saving = true;
      this.api
        .approveJournalEntry(entry.id, this.currentUser())
        .pipe(finalize(() => (this.saving = false)))
        .subscribe({
          next: () => {
            this.showSuccess('JOURNAL.APPROVE_SUCCESS');
            this.formVisible = false;
            this.loadEntries();
          },
          error: (err) => {
            const msg = err?.error?.message;
            this.showError(msg && msg !== 'COMMON.OK' ? msg : 'JOURNAL.APPROVE_ERROR');
          }
        });
    });
  }

  private reverseEntry(entry: JournalEntry): void {
    if (entry.status !== 'APPROVED') {
      this.showError('JOURNAL.REVERSE_ONLY_APPROVED');
      return;
    }
    this.confirmDialog.confirmByKey({ messageKey: 'JOURNAL.CONFIRM_REVERSE', danger: true }).subscribe((ok) => {
      if (ok !== true) {
        return;
      }
      this.saving = true;
      this.api
        .reverseJournalEntry(entry.id, this.currentUser(), this.translationService.instant('JOURNAL.REVERSE_REASON_DEFAULT'))
        .pipe(finalize(() => (this.saving = false)))
        .subscribe({
          next: () => {
            this.showSuccess('JOURNAL.REVERSE_SUCCESS');
            this.loadEntries();
          },
          error: (err) => {
            const msg = err?.error?.message;
            this.showError(msg && msg !== 'COMMON.OK' ? msg : 'JOURNAL.REVERSE_ERROR');
          }
        });
    });
  }

  private showError(key: string): void {
    this.errorKey = key;
    this.successKey = '';
    this.queueFeedbackClear();
  }

  private showSuccess(key: string): void {
    this.successKey = key;
    this.errorKey = '';
    this.queueFeedbackClear();
  }

  private queueFeedbackClear(): void {
    if (this.feedbackTimer) {
      clearTimeout(this.feedbackTimer);
    }
    this.feedbackTimer = setTimeout(() => {
      this.errorKey = '';
      this.successKey = '';
      this.feedbackTimer = null;
    }, 5000);
  }

  private currentUser(): string {
    return this.actorEmail || 'system@erp.local';
  }

  private accountName(line: JournalEntryLine): string {
    return this.translationService.currentLanguage === 'ar' ? (line.accountNameAr || line.accountNameEn || '') : (line.accountNameEn || line.accountNameAr || '');
  }
}
