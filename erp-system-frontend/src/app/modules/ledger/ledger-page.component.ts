import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { finalize } from 'rxjs/operators';
import { AccountDto, LedgerDto } from '../../core/models/accounting.models';
import { TranslationService } from '../../core/i18n/translation.service';
import { AccountingApiService } from '../../core/services/accounting-api.service';
@Component({
  standalone: false,
  selector: 'app-ledger-page',
  templateUrl: './ledger-page.component.html',
  styleUrls: ['./ledger-page.component.scss']
})
export class LedgerPageComponent implements OnInit {
  titleKey = 'NAV.LEDGER';
  loading = false;
  errorKey = '';
  rows: Array<Record<string, unknown>> = [];
  accounts: AccountDto[] = [];
  ledger: LedgerDto | null = null;
  columns = [
    { key: 'journalReference', title: 'LEDGER.REFERENCE', align: 'start' as 'start' },
    { key: 'entryDate', title: 'LEDGER.DATE', kind: 'date' as 'date' },
    { key: 'description', title: 'LEDGER.DESCRIPTION', align: 'start' as 'start' },
    { key: 'debit', title: 'LEDGER.DEBIT', align: 'end' as 'end' },
    { key: 'credit', title: 'LEDGER.CREDIT', align: 'end' as 'end' },
    { key: 'runningBalance', title: 'LEDGER.RUNNING_BALANCE', align: 'end' as 'end' }
  ];

  readonly form = this.fb.group({
    accountId: [null as number | null, Validators.required],
    fromDate: [''],
    toDate: ['']
  });

  constructor(
    private api: AccountingApiService,
    private fb: FormBuilder,
    private translationService: TranslationService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    const today = new Date().toISOString().slice(0, 10);
    const monthStart = `${today.slice(0, 8)}01`;
    this.form.patchValue({ fromDate: monthStart, toDate: today });

    this.api.getAccounts({ active: true }).subscribe(
      (accounts) => {
        this.accounts = accounts.filter((account) => account.postable);
        if (this.accounts.length) {
          this.form.patchValue({ accountId: this.accounts[0].id });
          this.load();
        }
      },
      () => { this.errorKey = 'COMMON.ERROR_LOADING'; }
    );
  }

  get selectedAccount(): AccountDto | null {
    const accountId = Number(this.form.controls.accountId.value || 0);
    return this.accounts.find((account) => account.id === accountId) || null;
  }

  get selectedAccountLabel(): string {
    const account = this.selectedAccount;
    if (!account) {
      return '';
    }
    const localizedName =
      this.translationService.currentLanguage === 'ar'
        ? account.nameAr || account.nameEn || account.name
        : account.nameEn || account.nameAr || account.name;
    return `${account.code} - ${localizedName}`;
  }

  get accountOptions(): Array<{ id: number; label: string }> {
    return this.accounts.map((account) => ({
      id: account.id,
      label:
        this.translationService.currentLanguage === 'ar'
          ? `${account.code} - ${account.nameAr || account.nameEn || account.name}`
          : `${account.code} - ${account.nameEn || account.nameAr || account.name}`
    }));
  }

  load(): void {
    const accountId = Number(this.form.controls.accountId.value || 0);
    if (!accountId) {
      this.ledger = null;
      this.rows = [];
      return;
    }
    this.loading = true;
    this.errorKey = '';
    this.api
      .getLedger({
        accountId,
        fromDate: this.form.controls.fromDate.value || '',
        toDate: this.form.controls.toDate.value || ''
      })
      .pipe(
        finalize(() => {
          this.loading = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe(
        (ledger: LedgerDto) => {
          this.ledger = ledger;
          this.rows = (ledger.lines || []).map((line) => ({
            ...line,
            description: line.description || this.translationService.instant('LEDGER.NO_DESCRIPTION'),
            debit: Number(line.debit || 0).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 }),
            credit: Number(line.credit || 0).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 }),
            runningBalance: Number(line.runningBalance || 0).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })
          }));
        },
        () => {
          this.ledger = null;
          this.errorKey = 'COMMON.ERROR_LOADING';
          this.rows = [];
        }
      );
  }
}

