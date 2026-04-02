import { Component, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { finalize } from 'rxjs/operators';
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

@Component({ standalone: false,
  selector: 'app-settings-page',
  templateUrl: './settings-page.component.html',
  styleUrls: ['./settings-page.component.scss']
})
export class SettingsPageComponent implements OnInit {
  titleKey = 'SETTINGS.TITLE';
  activeTabId = 'accounting';
  loading = false;
  accountingLoading = false;
  saving = false;
  accountingSaving = false;
  fiscalYearSaving = false;
  fiscalPeriodSaving = false;
  errorKey = '';
  successKey = '';
  accountingErrorKey = '';
  accountingSuccessKey = '';
  user: AuthUser | null = null;
  imagePreview = '';
  themeMode: ThemeMode = 'light';
  actorEmail = 'frontend.user';
  accountingMethodOptions: LookupItem[] = [];
  currencyOptions: LookupItem[] = [];
  accountingSettings: AccountingSettingsDto | null = null;
  selectedAllowedCurrencies: string[] = [];

  form = this.fb.group({
    fullName: ['', [Validators.required, Validators.maxLength(150)]],
    username: ['', [Validators.required, Validators.minLength(4), Validators.maxLength(100)]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(190)]],
    phone: ['', [Validators.required, Validators.maxLength(30)]],
    nationalId: ['', [Validators.maxLength(60)]],
    companyName: ['', [Validators.maxLength(180)]],
    profileImage: ['']
  });

  accountingForm = this.fb.group({
    accountingMethod: ['', Validators.required],
    baseCurrency: ['', Validators.required]
  });

  fiscalYearForm = this.fb.group({
    year: [new Date().getFullYear(), [Validators.required, Validators.min(2000)]],
    startDate: ['', Validators.required],
    endDate: ['', Validators.required]
  });

  fiscalPeriodForm = this.fb.group({
    fiscalYearId: [null as number | null, Validators.required],
    periodName: ['', Validators.required],
    startDate: ['', Validators.required],
    endDate: ['', Validators.required]
  });

  constructor(
    private authService: AuthService,
    private accountingApi: AccountingApiService,
    private fb: FormBuilder,
    private lookupService: LookupService,
    private themeService: ThemeService,
    public translationService: TranslationService
  ) {}

  ngOnInit(): void {
    this.themeMode = this.themeService.mode;
    this.authService.currentUser$.subscribe((user) => {
      this.user = user;
      this.actorEmail = user?.email || user?.username || 'frontend.user';
    });
    this.authService.refreshCurrentUser();
    this.loadProfile();
    this.loadAccountingContext();
  }

  onSelectImage(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files && input.files.length ? input.files[0] : null;
    if (!file) {
      return;
    }
    const reader = new FileReader();
    reader.onload = () => {
      const data = (reader.result || '').toString();
      this.form.patchValue({ profileImage: data });
      this.imagePreview = data;
    };
    reader.readAsDataURL(file);
  }

  save(): void {
    if (this.form.invalid || this.saving) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving = true;
    this.errorKey = '';
    this.successKey = '';
    const payload: UpdateProfileRequest = {
      username: (this.form.value.username || '').trim(),
      email: (this.form.value.email || '').trim(),
      phone: (this.form.value.phone || '').trim(),
      fullName: (this.form.value.fullName || '').trim(),
      nationalId: (this.form.value.nationalId || '').trim() || null,
      companyName: (this.form.value.companyName || '').trim() || null,
      profileImage: (this.form.value.profileImage || '').trim() || null
    };
    this.authService.updateMyProfile(payload).subscribe(
      (user) => {
        this.user = user;
        this.imagePreview = user.profile?.profileImage || '';
        this.successKey = 'PROFILE.SAVE_SUCCESS';
        this.saving = false;
      },
      () => {
        this.errorKey = 'PROFILE.SAVE_ERROR';
        this.saving = false;
      }
    );
  }

  saveAccountingSettings(): void {
    if (this.accountingForm.invalid || this.accountingSaving) {
      this.accountingForm.markAllAsTouched();
      return;
    }

    const baseCurrency = String(this.accountingForm.value.baseCurrency || '').trim().toUpperCase();
    if (!this.selectedAllowedCurrencies.length) {
      this.accountingErrorKey = 'SETTINGS.ALLOWED_CURRENCIES_REQUIRED';
      return;
    }

    if (!this.selectedAllowedCurrencies.includes(baseCurrency)) {
      this.selectedAllowedCurrencies = [...this.selectedAllowedCurrencies, baseCurrency];
    }

    this.accountingSaving = true;
    this.accountingErrorKey = '';
    this.accountingSuccessKey = '';

    const payload: AccountingSettingsUpdateDto = {
      accountingMethod: String(this.accountingForm.value.accountingMethod || ''),
      baseCurrency,
      allowedCurrencies: this.selectedAllowedCurrencies.join(',')
    };

    this.accountingApi
      .updateSettings(payload)
      .pipe(finalize(() => (this.accountingSaving = false)))
      .subscribe(
        (settings) => {
          this.applyAccountingSettings(settings);
          this.accountingSuccessKey = 'SETTINGS.SAVE_SUCCESS';
        },
        () => {
          this.accountingErrorKey = 'SETTINGS.SAVE_ERROR';
        }
      );
  }

  createFiscalYear(): void {
    if (this.fiscalYearForm.invalid || this.fiscalYearSaving) {
      this.fiscalYearForm.markAllAsTouched();
      return;
    }

    this.fiscalYearSaving = true;
    this.accountingErrorKey = '';
    this.accountingSuccessKey = '';

    const payload: FiscalYearFormDto = {
      year: Number(this.fiscalYearForm.value.year || 0),
      startDate: String(this.fiscalYearForm.value.startDate || ''),
      endDate: String(this.fiscalYearForm.value.endDate || '')
    };

    this.accountingApi
      .createFiscalYear(payload)
      .pipe(finalize(() => (this.fiscalYearSaving = false)))
      .subscribe(
        () => {
          this.accountingSuccessKey = 'SETTINGS.CREATE_SUCCESS';
          this.fiscalYearForm.patchValue({ year: new Date().getFullYear(), startDate: '', endDate: '' });
          this.refreshAccountingSettings();
        },
        (err) => {
          const msg = err?.error?.message;
          this.accountingErrorKey = msg && msg !== 'COMMON.OK' ? msg : 'SETTINGS.ACTION_ERROR';
        }
      );
  }

  createFiscalPeriod(): void {
    if (this.fiscalPeriodForm.invalid || this.fiscalPeriodSaving) {
      this.fiscalPeriodForm.markAllAsTouched();
      return;
    }

    const fiscalYearId = Number(this.fiscalPeriodForm.value.fiscalYearId || 0);
    if (!fiscalYearId) {
      this.accountingErrorKey = 'SETTINGS.SELECT_FISCAL_YEAR';
      return;
    }

    this.fiscalPeriodSaving = true;
    this.accountingErrorKey = '';
    this.accountingSuccessKey = '';

    const payload: FiscalPeriodFormDto = {
      periodName: String(this.fiscalPeriodForm.value.periodName || '').trim(),
      startDate: String(this.fiscalPeriodForm.value.startDate || ''),
      endDate: String(this.fiscalPeriodForm.value.endDate || '')
    };

    this.accountingApi
      .createFiscalPeriod(fiscalYearId, payload)
      .pipe(finalize(() => (this.fiscalPeriodSaving = false)))
      .subscribe(
        () => {
          this.accountingSuccessKey = 'SETTINGS.CREATE_SUCCESS';
          this.fiscalPeriodForm.patchValue({ periodName: '', startDate: '', endDate: '' });
          this.refreshAccountingSettings();
        },
        () => {
          this.accountingErrorKey = 'SETTINGS.ACTION_ERROR';
        }
      );
  }

  setTheme(mode: ThemeMode): void {
    this.themeMode = mode;
    this.themeService.setMode(mode);
  }

  setLanguage(lang: string): void {
    if (this.translationService.currentLanguage === lang) {
      return;
    }
    this.translationService.setLanguage(lang).subscribe();
  }

  toggleAllowedCurrency(code: string, checked: boolean): void {
    if (checked) {
      if (!this.selectedAllowedCurrencies.includes(code)) {
        this.selectedAllowedCurrencies = [...this.selectedAllowedCurrencies, code];
      }
      return;
    }

    this.selectedAllowedCurrencies = this.selectedAllowedCurrencies.filter((item) => item !== code);
    if (this.accountingForm.value.baseCurrency === code) {
      this.accountingForm.patchValue({
        baseCurrency: this.selectedAllowedCurrencies[0] || ''
      });
    }
  }

  isCurrencySelected(code: string): boolean {
    return this.selectedAllowedCurrencies.includes(code);
  }

  fiscalToggling = false;

  toggleFiscalYear(year: FiscalYearDto): void {
    if (this.fiscalToggling) { return; }
    const confirmKey = year.open ? 'SETTINGS.CONFIRM_CLOSE_FISCAL_YEAR' : 'SETTINGS.CONFIRM_OPEN_FISCAL_YEAR';
    if (!window.confirm(this.translationService.instant(confirmKey))) {
      return;
    }

    this.accountingErrorKey = '';
    this.accountingSuccessKey = '';
    this.fiscalToggling = true;
    const request$ = year.open
      ? this.accountingApi.closeFiscalYear(year.id, this.actorEmail)
      : this.accountingApi.openFiscalYear(year.id);

    request$.pipe(finalize(() => (this.fiscalToggling = false))).subscribe(
      () => {
        this.accountingSuccessKey = 'SETTINGS.ACTION_SUCCESS';
        this.refreshAccountingSettings();
      },
      (err) => {
        const msg = err?.error?.message;
        this.accountingErrorKey = msg && msg !== 'COMMON.OK' ? msg : 'SETTINGS.ACTION_ERROR';
      }
    );
  }

  toggleFiscalPeriod(period: FiscalPeriodDto): void {
    if (this.fiscalToggling) { return; }
    const confirmKey = period.open ? 'SETTINGS.CONFIRM_CLOSE_FISCAL_PERIOD' : 'SETTINGS.CONFIRM_OPEN_FISCAL_PERIOD';
    if (!window.confirm(this.translationService.instant(confirmKey))) {
      return;
    }

    this.accountingErrorKey = '';
    this.accountingSuccessKey = '';
    this.fiscalToggling = true;
    const request$ = period.open
      ? this.accountingApi.closeFiscalPeriod(period.id, this.actorEmail)
      : this.accountingApi.openFiscalPeriod(period.id);

    request$.pipe(finalize(() => (this.fiscalToggling = false))).subscribe(
      () => {
        this.accountingSuccessKey = 'SETTINGS.ACTION_SUCCESS';
        this.refreshAccountingSettings();
      },
      (err) => {
        const msg = err?.error?.message;
        this.accountingErrorKey = msg && msg !== 'COMMON.OK' ? msg : 'SETTINGS.ACTION_ERROR';
      }
    );
  }

  allowedCurrenciesLabel(): string {
    return this.selectedAllowedCurrencies.join(', ') || '-';
  }

  get fiscalYears(): FiscalYearDto[] {
    return (this.accountingSettings && this.accountingSettings.fiscalYears) || [];
  }

  private loadProfile(): void {
    this.loading = true;
    this.errorKey = '';
    this.successKey = '';
    this.authService.getMyProfile().subscribe(
      (user) => {
        this.imagePreview = user.profile?.profileImage || '';
        this.form.patchValue({
          fullName: user.profile?.fullName || '',
          username: user.username || '',
          email: user.email || '',
          phone: user.phone || '',
          nationalId: user.profile?.nationalId || '',
          companyName: user.profile?.companyName || '',
          profileImage: user.profile?.profileImage || ''
        });
        this.loading = false;
      },
      () => {
        this.errorKey = 'PROFILE.LOAD_ERROR';
        this.loading = false;
      }
    );
  }

  private loadAccountingContext(): void {
    this.accountingLoading = true;
    this.accountingErrorKey = '';
    this.accountingSuccessKey = '';

    forkJoin({
      methods: this.lookupService.getLookup('accounting-methods'),
      currencies: this.lookupService.getLookup('currencies'),
      settings: this.accountingApi.getSettings()
    })
      .pipe(finalize(() => (this.accountingLoading = false)))
      .subscribe(
        (result) => {
          this.accountingMethodOptions = result.methods || [];
          this.currencyOptions = result.currencies || [];
          this.applyAccountingSettings(result.settings);
        },
        () => {
          this.accountingErrorKey = 'SETTINGS.LOAD_ERROR';
        }
      );
  }

  private refreshAccountingSettings(): void {
    this.accountingLoading = true;
    this.accountingApi
      .getSettings()
      .pipe(finalize(() => (this.accountingLoading = false)))
      .subscribe(
        (settings) => this.applyAccountingSettings(settings),
        () => {
          this.accountingErrorKey = 'SETTINGS.LOAD_ERROR';
        }
      );
  }

  private applyAccountingSettings(settings: AccountingSettingsDto): void {
    this.accountingSettings = settings;
    this.selectedAllowedCurrencies = String(settings.allowedCurrencies || '')
      .split(',')
      .map((item) => item.trim())
      .filter((item) => !!item);

    if (settings.baseCurrency && !this.selectedAllowedCurrencies.includes(settings.baseCurrency)) {
      this.selectedAllowedCurrencies = [...this.selectedAllowedCurrencies, settings.baseCurrency];
    }

    this.accountingForm.patchValue({
      accountingMethod: settings.accountingMethod || this.accountingMethodOptions[0]?.code || '',
      baseCurrency: settings.baseCurrency || this.selectedAllowedCurrencies[0] || this.currencyOptions[0]?.code || ''
    });

    const defaultFiscalYear = this.fiscalYears.find((item) => item.open) || this.fiscalYears[0] || null;
    this.fiscalPeriodForm.patchValue({
      fiscalYearId: defaultFiscalYear ? defaultFiscalYear.id : null
    });
  }
}
