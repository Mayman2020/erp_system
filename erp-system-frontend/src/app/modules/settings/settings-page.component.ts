import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { forkJoin, Subject } from 'rxjs';
import { finalize, takeUntil } from 'rxjs/operators';
import { AuthService, AuthUser, UpdateProfileRequest } from '../../core/auth/auth.service';
import { TranslationService } from '../../core/i18n/translation.service';
import {
  AccountingSettingsDto,
  AccountingSettingsUpdateDto,
  FiscalPeriodDto,
  FiscalPeriodFormDto,
  FiscalYearDto,
  FiscalYearFormDto
} from '../../core/models/accounting.models';
import { LookupItem } from '../../core/models/lookup.models';
import { AccountingApiService } from '../../core/services/accounting-api.service';
import { LookupService } from '../../core/services/lookup.service';
import { ThemeMode, ThemeService } from '../../core/services/theme.service';
import { ConfirmDialogService } from '../../core/services/confirm-dialog.service';

@Component({ standalone: false,
  selector: 'app-settings-page',
  templateUrl: './settings-page.component.html',
  styleUrls: ['./settings-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SettingsPageComponent implements OnInit, OnDestroy {
  titleKey = 'SETTINGS.TITLE';
  activeTabId = 'profile'; // Default to profile as requested
  loading = false;
  accountingLoading = false;
  saving = false;
  accountingSavingLabel = '';
  
  errorKey = '';
  successKey = '';
  accountingErrorKey = '';
  accountingSuccessKey = '';

  actorEmail = 'frontend.user';
  public themeMode: ThemeMode = 'light';
  public currentLanguage = 'ar';
  
  public readonly user$ = this.authService.currentUser$;
  private readonly destroy$ = new Subject<void>();
  
  accountingMethodOptions: LookupItem[] = [];
  currencyOptions: LookupItem[] = [];
  accountingSettings: AccountingSettingsDto | null = null;

  constructor(
    private authService: AuthService,
    private accountingApi: AccountingApiService,
    private lookupService: LookupService,
    private themeService: ThemeService,
    public translationService: TranslationService,
    private confirmDialog: ConfirmDialogService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.themeMode = this.themeService.mode;
    this.currentLanguage = this.translationService.currentLanguage;
    
    this.user$.pipe(takeUntil(this.destroy$)).subscribe((user) => {
      if (user) {
        this.actorEmail = user.email || user.username || 'frontend.user';
      }
      this.cdr.markForCheck();
    });

    this.authService.refreshCurrentUser();
    this.loadAccountingContext();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  onSaveProfile(data: UpdateProfileRequest): void {
    this.saving = true;
    this.errorKey = '';
    this.successKey = '';
    
    this.authService.updateMyProfile(data).pipe(
      finalize(() => {
        this.saving = false;
        this.cdr.markForCheck();
      })
    ).subscribe({
      next: () => this.successKey = 'PROFILE.SAVE_SUCCESS',
      error: () => this.errorKey = 'PROFILE.SAVE_ERROR'
    });
  }

  onSaveAccounting(data: any): void {
    this.accountingSavingLabel = 'SETTINGS.SAVING_ACCOUNTING';
    this.accountingErrorKey = '';
    this.accountingSuccessKey = '';

    this.accountingApi.updateSettings(data).pipe(
      finalize(() => {
        this.accountingSavingLabel = '';
        this.cdr.markForCheck();
      })
    ).subscribe({
      next: (settings) => {
        this.accountingSettings = settings;
        this.accountingSuccessKey = 'SETTINGS.SAVE_SUCCESS';
      },
      error: () => this.accountingErrorKey = 'SETTINGS.SAVE_ERROR'
    });
  }

  onToggleYear(year: FiscalYearDto): void {
    const confirmKey = year.open ? 'SETTINGS.CONFIRM_CLOSE_FISCAL_YEAR' : 'SETTINGS.CONFIRM_OPEN_FISCAL_YEAR';
    this.confirmDialog.confirmByKey({ messageKey: confirmKey }).subscribe((ok) => {
      if (!ok) return;

      this.accountingLoading = true;
      const request$ = year.open
        ? this.accountingApi.closeFiscalYear(year.id, this.actorEmail)
        : this.accountingApi.openFiscalYear(year.id);

      request$.pipe(finalize(() => {
        this.accountingLoading = false;
        this.cdr.markForCheck();
      })).subscribe({
        next: () => this.refreshAccountingSettings(),
        error: (err) => this.accountingErrorKey = err?.error?.message || 'SETTINGS.ACTION_ERROR'
      });
    });
  }

  onTogglePeriod(period: FiscalPeriodDto): void {
    const confirmKey = period.open ? 'SETTINGS.CONFIRM_CLOSE_FISCAL_PERIOD' : 'SETTINGS.CONFIRM_OPEN_FISCAL_PERIOD';
    this.confirmDialog.confirmByKey({ messageKey: confirmKey }).subscribe((ok) => {
      if (!ok) return;

      this.accountingLoading = true;
      const request$ = period.open
        ? this.accountingApi.closeFiscalPeriod(period.id, this.actorEmail)
        : this.accountingApi.openFiscalPeriod(period.id);

      request$.pipe(finalize(() => {
        this.accountingLoading = false;
        this.cdr.markForCheck();
      })).subscribe({
        next: () => this.refreshAccountingSettings(),
        error: () => this.accountingErrorKey = 'SETTINGS.ACTION_ERROR'
      });
    });
  }

  onCreateYear(data: FiscalYearFormDto): void {
    this.accountingLoading = true;
    this.accountingApi.createFiscalYear(data).pipe(
      finalize(() => {
        this.accountingLoading = false;
        this.cdr.markForCheck();
      })
    ).subscribe({
      next: () => {
        this.accountingSuccessKey = 'SETTINGS.CREATE_SUCCESS';
        this.refreshAccountingSettings();
      },
      error: (err) => this.accountingErrorKey = err?.error?.message || 'SETTINGS.ACTION_ERROR'
    });
  }

  onCreatePeriod(data: any): void {
    const { fiscalYearId, ...periodData } = data;
    this.accountingLoading = true;
    this.accountingApi.createFiscalPeriod(fiscalYearId, periodData as FiscalPeriodFormDto).pipe(
      finalize(() => {
        this.accountingLoading = false;
        this.cdr.markForCheck();
      })
    ).subscribe({
      next: () => {
        this.accountingSuccessKey = 'SETTINGS.CREATE_SUCCESS';
        this.refreshAccountingSettings();
      },
      error: () => this.accountingErrorKey = 'SETTINGS.ACTION_ERROR'
    });
  }

  setTheme(mode: ThemeMode): void {
    this.themeMode = mode;
    this.themeService.setMode(mode);
    this.cdr.markForCheck();
  }

  setLanguage(lang: string): void {
    if (this.currentLanguage === lang) return;
    this.translationService.setLanguage(lang).subscribe(() => {
      this.currentLanguage = lang;
      this.cdr.markForCheck();
    });
  }

  private loadAccountingContext(): void {
    this.accountingLoading = true;
    forkJoin({
      methods: this.lookupService.getLookup('accounting-methods'),
      currencies: this.lookupService.getLookup('currencies'),
      settings: this.accountingApi.getSettings()
    }).pipe(
      finalize(() => {
        this.accountingLoading = false;
        this.cdr.markForCheck();
      })
    ).subscribe({
      next: (res) => {
        this.accountingMethodOptions = res.methods || [];
        this.currencyOptions = res.currencies || [];
        this.accountingSettings = res.settings;
      }
    });
  }

  private refreshAccountingSettings(): void {
    this.accountingApi.getSettings().subscribe(s => {
      this.accountingSettings = s;
      this.cdr.markForCheck();
    });
  }
}
