import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { finalize } from 'rxjs/operators';
import { JournalEntry } from '../../core/models/accounting.models';
import { TranslationService } from '../../core/i18n/translation.service';
import { AccountingApiService } from '../../core/services/accounting-api.service';

@Component({
  standalone: false,
  selector: 'app-general-ledger-detail',
  templateUrl: './general-ledger-detail.component.html',
  styleUrls: ['./general-ledger-detail.component.scss']
})
export class GeneralLedgerDetailComponent implements OnInit {
  loading = false;
  errorKey = '';
  entry: JournalEntry | null = null;

  constructor(
    private api: AccountingApiService,
    private route: ActivatedRoute,
    private router: Router,
    private i18n: TranslationService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) {
      this.router.navigate(['/general-ledger']);
      return;
    }
    this.loadEntry(id);
  }

  goBack(): void {
    this.router.navigate(['/general-ledger']);
  }

  get sourceLabel(): string {
    if (!this.entry?.sourceModule) {
      return this.i18n.instant('GENERAL_LEDGER.SOURCE_MANUAL');
    }
    return this.i18n.instant('GENERAL_LEDGER.SOURCE_' + this.entry.sourceModule);
  }

  formatAmount(val: number): string {
    return Number(val || 0).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  }

  private loadEntry(id: number): void {
    this.loading = true;
    this.errorKey = '';
    this.api
      .getJournalEntry(id)
      .pipe(finalize(() => { this.loading = false; this.cdr.detectChanges(); }))
      .subscribe({
        next: (entry) => { this.entry = entry; },
        error: () => { this.errorKey = 'COMMON.ERROR_LOADING'; }
      });
  }
}
