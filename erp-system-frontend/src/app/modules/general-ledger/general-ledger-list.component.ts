import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { finalize } from 'rxjs/operators';
import { JournalEntry } from '../../core/models/accounting.models';
import { TranslationService } from '../../core/i18n/translation.service';
import { AccountingApiService } from '../../core/services/accounting-api.service';
import { DataTableColumn, DataTableAction } from '../../shared/components/data-table/data-table.component';

@Component({
  standalone: false,
  selector: 'app-general-ledger-list',
  templateUrl: './general-ledger-list.component.html',
  styleUrls: ['./general-ledger-list.component.scss']
})
export class GeneralLedgerListComponent implements OnInit {
  loading = false;
  errorKey = '';
  entries: JournalEntry[] = [];
  rows: Array<Record<string, unknown>> = [];

  filters = {
    query: '',
    status: '',
    fromDate: '',
    toDate: ''
  };

  readonly columns: DataTableColumn[] = [
    { key: 'referenceNumber', title: 'GENERAL_LEDGER.REFERENCE', align: 'start', clickable: true },
    { key: 'description', title: 'GENERAL_LEDGER.DESCRIPTION', align: 'start', clickable: true },
    { key: 'entryDate', title: 'GENERAL_LEDGER.DATE' },
    { key: 'sourceLabel', title: 'GENERAL_LEDGER.SOURCE_TYPE' },
    { key: 'totalDebit', title: 'GENERAL_LEDGER.TOTAL_DEBIT', align: 'end' },
    { key: 'totalCredit', title: 'GENERAL_LEDGER.TOTAL_CREDIT', align: 'end' },
    { key: 'status', title: 'COMMON.STATUS', kind: 'status' },
    { key: 'createdAt', title: 'GENERAL_LEDGER.CREATED_AT', kind: 'date' },
    { key: 'createdBy', title: 'GENERAL_LEDGER.CREATED_BY', align: 'start' }
  ];

  readonly actions: DataTableAction[] = [
    { id: 'view', labelKey: 'COMMON.VIEW', className: 'erp-action-secondary' }
  ];

  readonly statusOptions = ['DRAFT', 'APPROVED', 'REVERSED', 'CANCELLED'];

  constructor(
    private api: AccountingApiService,
    private router: Router,
    private i18n: TranslationService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.load();
  }

  onSearch(searchValue?: Record<string, string>): void {
    this.filters.query = searchValue?.query || '';
    this.filters.status = searchValue?.status || '';
    this.filters.fromDate = searchValue?.fromDate || '';
    this.filters.toDate = searchValue?.toDate || '';
    this.load();
  }

  load(): void {
    this.loading = true;
    this.errorKey = '';
    this.api
      .getJournalEntries({
        search: this.filters.query,
        status: this.filters.status,
        fromDate: this.filters.fromDate,
        toDate: this.filters.toDate
      })
      .pipe(finalize(() => { this.loading = false; this.cdr.detectChanges(); }))
      .subscribe({
        next: (items) => {
          this.entries = items;
          this.rows = items.map((e) => this.toRow(e));
        },
        error: () => {
          this.errorKey = 'COMMON.ERROR_LOADING';
          this.rows = [];
        }
      });
  }

  onTableAction(event: { actionId: string; row: Record<string, unknown> }): void {
    if (event.actionId === 'view') {
      const id = Number(event.row['id']);
      if (id) {
        this.router.navigate(['/general-ledger', id]);
      }
    }
  }

  onCellClick(event: { key: string; row: Record<string, unknown> }): void {
    const id = Number(event.row['id']);
    if (id) {
      this.router.navigate(['/general-ledger', id]);
    }
  }

  private toRow(entry: JournalEntry): Record<string, unknown> {
    return {
      id: entry.id,
      referenceNumber: entry.referenceNumber,
      description: entry.description || '',
      entryDate: entry.entryDate,
      sourceLabel: this.i18n.instant('GENERAL_LEDGER.SOURCE_' + (entry.sourceModule || 'MANUAL')),
      totalDebit: this.formatAmount(entry.totalDebit),
      totalCredit: this.formatAmount(entry.totalCredit),
      status: entry.status,
      createdAt: entry.createdAt || '',
      createdBy: entry.createdBy || ''
    };
  }

  private formatAmount(val: number): string {
    return Number(val || 0).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  }
}
