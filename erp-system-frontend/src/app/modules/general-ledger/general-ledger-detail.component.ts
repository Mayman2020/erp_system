import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { Location } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { BehaviorSubject, Observable } from 'rxjs';
import { finalize } from 'rxjs/operators';
import { JournalEntry } from '../../core/models/accounting.models';
import { TranslationService } from '../../core/i18n/translation.service';
import { AccountingApiService } from '../../core/services/accounting-api.service';
import { NavigationHistoryService } from '../../core/services/navigation-history.service';

@Component({
  standalone: false,
  selector: 'app-general-ledger-detail',
  templateUrl: './general-ledger-detail.component.html',
  styleUrls: ['./general-ledger-detail.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class GeneralLedgerDetailComponent implements OnInit {
  loading = false;
  errorKey = '';
  
  private readonly entrySubject = new BehaviorSubject<JournalEntry | null>(null);
  public readonly entry$: Observable<JournalEntry | null> = this.entrySubject.asObservable();

  constructor(
    private api: AccountingApiService,
    private route: ActivatedRoute,
    private router: Router,
    private location: Location,
    private navHistory: NavigationHistoryService,
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
    if (this.navHistory.canGoBack()) {
      this.navHistory.goBack(this.location);
      return;
    }
    this.router.navigate(['/general-ledger']);
  }

  public resolveSourceLabel(entry: JournalEntry | null): string {
    if (!entry?.sourceModule) {
      return this.i18n.instant('GENERAL_LEDGER.SOURCE_MANUAL');
    }
    return this.i18n.instant('GENERAL_LEDGER.SOURCE_' + entry.sourceModule);
  }

  formatAmount(val: number): string {
    return Number(val || 0).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  }

  private loadEntry(id: number): void {
    this.loading = true;
    this.errorKey = '';
    this.api
      .getJournalEntry(id)
      .pipe(finalize(() => { 
        this.loading = false; 
        this.cdr.markForCheck(); 
      }))
      .subscribe({
        next: (entry) => { this.entrySubject.next(entry); },
        error: () => { this.errorKey = 'COMMON.ERROR_LOADING'; }
      });
  }
}
