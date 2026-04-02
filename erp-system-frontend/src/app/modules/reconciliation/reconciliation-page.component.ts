import { Component, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { finalize } from 'rxjs/operators';
import { ReconciliationBankAccountDto, ReconciliationDto, ReconciliationLineDto, ReconciliationSummaryDto } from '../../core/models/accounting.models';
import { AccountingApiService } from '../../core/services/accounting-api.service';
import { LookupService } from '../../core/services/lookup.service';

@Component({
  selector: 'app-reconciliation-page',
  templateUrl: './reconciliation-page.component.html',
  styleUrls: ['./reconciliation-page.component.scss']
})
export class ReconciliationPageComponent implements OnInit {
  readonly titleKey = 'RECONCILIATION.TITLE';
  loading = false;
  saving = false;
  errorKey = '';
  reconciliations: ReconciliationDto[] = [];
  selectedReconciliation: ReconciliationDto | null = null;
  statementLines: ReconciliationLineDto[] = [];
  systemLines: ReconciliationLineDto[] = [];
  summary: ReconciliationSummaryDto | null = null;
  bankAccounts: ReconciliationBankAccountDto[] = [];
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

  constructor(private api: AccountingApiService, private lookupService: LookupService, private fb: FormBuilder) {}

  ngOnInit(): void {
    this.lookupService.getLookup('reconciliation-statuses').subscribe((items) => (this.statuses = items.map((item) => item.code)), () => undefined);
    this.api.getReconciliationBankAccounts().subscribe((items) => (this.bankAccounts = items), () => undefined);
    this.load();
  }

  onSearch(searchValue?: Record<string, string>): void {
    this.load(searchValue?.status || '');
  }

  load(status = ''): void {
    this.loading = true;
    this.errorKey = '';
    const filters = status ? { status } : {};
    this.api
      .getReconciliations(filters)
      .pipe(finalize(() => (this.loading = false)))
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
    this.api
      .createReconciliation(payload)
      .pipe(finalize(() => (this.saving = false)))
      .subscribe(
        (item) => {
          this.selectedReconciliation = item;
          this.load();
          this.selectReconciliation(item.id);
        },
        () => undefined
      );
  }

  selectReconciliation(id: number): void {
    this.api.getReconciliation(id).subscribe(
      (rec) => {
        this.selectedReconciliation = rec;
        this.api.getReconciliationStatementLines(id).subscribe((rows) => (this.statementLines = rows), () => undefined);
        this.api.getReconciliationSystemTransactions(id).subscribe((rows) => (this.systemLines = rows), () => undefined);
        this.api.getReconciliationSummary(id).subscribe((summary) => (this.summary = summary), () => undefined);
      },
      () => undefined
    );
  }

  pickStatement(line: ReconciliationLineDto): void {
    this.selectedStatementLineId = line.id;
  }

  pickSystem(line: ReconciliationLineDto): void {
    this.selectedSystemLineId = line.id;
  }

  matchSelected(): void {
    if (!this.selectedReconciliation || !this.selectedStatementLineId || !this.selectedSystemLineId) {
      return;
    }
    this.api.matchReconciliationLines(this.selectedReconciliation.id, this.selectedStatementLineId, this.selectedSystemLineId).subscribe(
      (updated) => {
        this.selectedReconciliation = updated;
        this.selectReconciliation(updated.id);
        this.selectedStatementLineId = null;
        this.selectedSystemLineId = null;
      },
      () => undefined
    );
  }

  unmatchLine(line: ReconciliationLineDto): void {
    if (!this.selectedReconciliation) {
      return;
    }
    this.api.unmatchReconciliationLine(this.selectedReconciliation.id, line.id).subscribe(
      (updated) => {
        this.selectedReconciliation = updated;
        this.selectReconciliation(updated.id);
      },
      () => undefined
    );
  }

  finalizeReconciliation(): void {
    if (!this.selectedReconciliation) {
      return;
    }
    this.api.finalizeReconciliation(this.selectedReconciliation.id, this.currentUser()).subscribe(
      (updated) => {
        this.selectedReconciliation = updated;
        this.selectReconciliation(updated.id);
        this.load();
      },
      () => undefined
    );
  }

  amountClass(line: ReconciliationLineDto): string {
    if (line.status === 'MATCHED') {
      return 'text-success';
    }
    if (line.status === 'PARTIALLY_MATCHED') {
      return 'text-warning';
    }
    return 'text-danger';
  }

  private currentUser(): string {
    return localStorage.getItem('erp.user') || 'frontend.user';
  }
}

