import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { finalize } from 'rxjs/operators';
import { ReconciliationBankAccountDto, ReconciliationDto, ReconciliationLineDto, ReconciliationSummaryDto } from '../../core/models/accounting.models';
import { AccountingApiService } from '../../core/services/accounting-api.service';
import { ConfirmDialogService } from '../../core/services/confirm-dialog.service';
import { LookupService } from '../../core/services/lookup.service';

@Component({ standalone: false,
  selector: 'app-reconciliation-page',
  templateUrl: './reconciliation-page.component.html',
  styleUrls: ['./reconciliation-page.component.scss']
})
export class ReconciliationPageComponent implements OnInit {
  readonly titleKey = 'RECONCILIATION.TITLE';
  loading = false;
  saving = false;
  errorKey = '';
  successKey = '';
  showCreateForm = false;
  reconciliations: ReconciliationDto[] = [];
  selectedReconciliation: ReconciliationDto | null = null;
  statementLines: ReconciliationLineDto[] = [];
  systemLines: ReconciliationLineDto[] = [];
  summary: ReconciliationSummaryDto | null = null;
  bankAccounts: ReconciliationBankAccountDto[] = [];
  bankAccountOptions: Array<{ id: number; label: string }> = [];
  selectedStatementLineId: number | null = null;
  selectedSystemLineId: number | null = null;
  statuses: string[] = [];

  readonly createForm = this.fb.group({
    bankAccountId: [null as number | null, Validators.required],
    statementStartDate: ['', Validators.required],
    statementEndDate: ['', Validators.required],
    openingBalance: [null as number | null, Validators.required],
    closingBalance: [null as number | null, Validators.required]
  });

  constructor(
    private api: AccountingApiService,
    private lookupService: LookupService,
    private confirmDialog: ConfirmDialogService,
    private fb: FormBuilder,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.lookupService.getLookup('reconciliation-statuses').subscribe({
      next: (items) => (this.statuses = items.map((item) => item.code))
    });
    this.api.getReconciliationBankAccounts().subscribe({
      next: (items) => {
        this.bankAccounts = items;
        this.bankAccountOptions = items.map((item) => ({ id: item.id, label: `${item.bankName} - ${item.accountNumber}` }));
      },
      error: () => { this.errorKey = 'COMMON.ERROR_LOADING'; }
    });
    this.load();
  }

  get isEditable(): boolean {
    return !!this.selectedReconciliation && (this.selectedReconciliation.status === 'OPEN' || this.selectedReconciliation.status === 'IN_PROGRESS');
  }

  onSearch(searchValue?: Record<string, string>): void {
    this.load(searchValue?.status || '');
  }

  toggleCreateForm(): void {
    this.showCreateForm = !this.showCreateForm;
    if (this.showCreateForm) {
      this.createForm.reset();
    }
  }

  load(status = ''): void {
    this.loading = true;
    this.errorKey = '';
    this.successKey = '';
    const filters = status ? { status } : {};
    this.api
      .getReconciliations(filters)
      .pipe(
        finalize(() => {
          this.loading = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: (items) => {
          this.reconciliations = items;
          if (!this.selectedReconciliation && items.length) {
            this.selectReconciliation(items[0].id);
          }
        },
        error: () => {
          this.errorKey = 'COMMON.ERROR_LOADING';
        }
      });
  }

  createReconciliation(): void {
    if (this.createForm.invalid) {
      this.createForm.markAllAsTouched();
      return;
    }
    const raw = this.createForm.getRawValue();
    const payload = {
      bankAccountId: Number(raw.bankAccountId || 0),
      statementStartDate: raw.statementStartDate || '',
      statementEndDate: raw.statementEndDate || '',
      openingBalance: Number(raw.openingBalance || 0),
      closingBalance: Number(raw.closingBalance || 0),
      statementLines: []
    };
    this.saving = true;
    this.errorKey = '';
    this.api
      .createReconciliation(payload)
      .pipe(finalize(() => (this.saving = false)))
      .subscribe({
        next: (item) => {
          this.selectedReconciliation = item;
          this.showCreateForm = false;
          this.successKey = 'RECONCILIATION.CREATE_SUCCESS';
          this.load();
          this.selectReconciliation(item.id);
        },
        error: () => {
          this.errorKey = 'RECONCILIATION.CREATE_ERROR';
        }
      });
  }

  selectReconciliation(id: number): void {
    this.errorKey = '';
    this.successKey = '';
    this.selectedStatementLineId = null;
    this.selectedSystemLineId = null;
    this.api.getReconciliation(id).subscribe({
      next: (rec) => {
        this.selectedReconciliation = rec;
        this.api.getReconciliationStatementLines(id).subscribe((rows) => (this.statementLines = rows));
        this.api.getReconciliationSystemTransactions(id).subscribe((rows) => (this.systemLines = rows));
        this.api.getReconciliationSummary(id).subscribe((s) => (this.summary = s));
      },
      error: () => { this.errorKey = 'COMMON.ERROR_LOADING'; }
    });
  }

  pickStatement(line: ReconciliationLineDto): void {
    if (line.status !== 'UNMATCHED') return;
    this.selectedStatementLineId = this.selectedStatementLineId === line.id ? null : line.id;
  }

  pickSystem(line: ReconciliationLineDto): void {
    if (line.status !== 'UNMATCHED') return;
    this.selectedSystemLineId = this.selectedSystemLineId === line.id ? null : line.id;
  }

  matchSelected(): void {
    if (!this.selectedReconciliation || !this.selectedStatementLineId || !this.selectedSystemLineId) return;
    this.errorKey = '';
    this.api.matchReconciliationLines(this.selectedReconciliation.id, this.selectedStatementLineId, this.selectedSystemLineId).subscribe({
      next: (updated) => {
        this.selectedReconciliation = updated;
        this.selectReconciliation(updated.id);
        this.successKey = 'RECONCILIATION.MATCH_SUCCESS';
      },
      error: () => { this.errorKey = 'RECONCILIATION.MATCH_ERROR'; }
    });
  }

  unmatchLine(line: ReconciliationLineDto): void {
    if (!this.selectedReconciliation) return;
    this.errorKey = '';
    this.api.unmatchReconciliationLine(this.selectedReconciliation.id, line.id).subscribe({
      next: (updated) => {
        this.selectedReconciliation = updated;
        this.selectReconciliation(updated.id);
        this.successKey = 'RECONCILIATION.UNMATCH_SUCCESS';
      },
      error: () => { this.errorKey = 'RECONCILIATION.UNMATCH_ERROR'; }
    });
  }

  finalizeReconciliation(): void {
    if (!this.selectedReconciliation) return;
    this.confirmDialog.confirmByKey({ messageKey: 'RECONCILIATION.CONFIRM_FINALIZE' }).subscribe((ok) => {
      if (ok !== true || !this.selectedReconciliation) return;
      this.errorKey = '';
      this.saving = true;
      this.api.finalizeReconciliation(this.selectedReconciliation.id, this.currentUser())
        .pipe(finalize(() => { this.saving = false; this.cdr.detectChanges(); }))
        .subscribe({
          next: (updated) => {
            this.selectedReconciliation = updated;
            this.selectReconciliation(updated.id);
            this.successKey = 'RECONCILIATION.FINALIZE_SUCCESS';
            this.load();
          },
          error: () => { this.errorKey = 'RECONCILIATION.FINALIZE_ERROR'; }
        });
    });
  }

  cancelReconciliation(): void {
    if (!this.selectedReconciliation) return;
    this.confirmDialog.confirmByKey({ messageKey: 'RECONCILIATION.CONFIRM_CANCEL', danger: true }).subscribe((ok) => {
      if (ok !== true || !this.selectedReconciliation) return;
      this.errorKey = '';
      this.saving = true;
      this.api.cancelReconciliation(this.selectedReconciliation.id, this.currentUser())
        .pipe(finalize(() => { this.saving = false; this.cdr.detectChanges(); }))
        .subscribe({
          next: (updated) => {
            this.selectedReconciliation = updated;
            this.selectReconciliation(updated.id);
            this.successKey = 'RECONCILIATION.CANCEL_SUCCESS';
            this.load();
          },
          error: () => { this.errorKey = 'RECONCILIATION.CANCEL_ERROR'; }
        });
    });
  }

  amountClass(line: ReconciliationLineDto): string {
    if (line.status === 'MATCHED') return 'text-success';
    if (line.status === 'PARTIALLY_MATCHED') return 'text-warning';
    return 'text-danger';
  }

  statusClass(status: string): string {
    switch (status) {
      case 'COMPLETED': return 'badge-success';
      case 'IN_PROGRESS': return 'badge-warning';
      case 'CANCELLED': return 'badge-danger';
      default: return 'badge-info';
    }
  }

  private currentUser(): string {
    return localStorage.getItem('erp.user') || 'frontend.user';
  }
}
