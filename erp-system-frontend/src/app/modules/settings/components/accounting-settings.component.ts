import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  EventEmitter,
  Input,
  OnDestroy,
  OnInit,
  Output
} from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { AccountingSettingsDto, FiscalPeriodDto, FiscalYearDto } from '../../../core/models/accounting.models';
import { LookupItem } from '../../../core/models/lookup.models';

@Component({
  standalone: false,
  selector: 'app-accounting-settings',
  templateUrl: './accounting-settings.component.html',
  styleUrls: ['./accounting-settings.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AccountingSettingsComponent implements OnInit, OnDestroy {
  @Input() settings: AccountingSettingsDto | null = null;
  @Input() methodOptions: LookupItem[] = [];
  @Input() currencyOptions: LookupItem[] = [];
  @Input() loading = false;
  @Input() saving = false;
  @Input() errorKey = '';
  @Input() successKey = '';

  @Output() saveSettings = new EventEmitter<any>();
  @Output() createYear = new EventEmitter<any>();
  @Output() createPeriod = new EventEmitter<any>();
  @Output() toggleYear = new EventEmitter<FiscalYearDto>();
  @Output() togglePeriod = new EventEmitter<FiscalPeriodDto>();

  accountingForm: FormGroup;
  fiscalYearForm: FormGroup;
  fiscalPeriodForm: FormGroup;
  selectedAllowedCurrencies: string[] = [];
  /** Year dropdown for new fiscal year (calendar years only). */
  readonly fiscalYearSelectOptions: number[] = (() => {
    const end = new Date().getFullYear() + 15;
    const out: number[] = [];
    for (let y = 1995; y <= end; y++) {
      out.push(y);
    }
    return out;
  })();

  private readonly destroy$ = new Subject<void>();

  constructor(
    private fb: FormBuilder,
    private cdr: ChangeDetectorRef
  ) {
    this.accountingForm = this.fb.group({
      accountingMethod: ['', Validators.required],
      baseCurrency: ['', Validators.required]
    });

    this.fiscalYearForm = this.fb.group({
      year: [new Date().getFullYear(), [Validators.required, Validators.min(2000)]],
      startDate: ['', Validators.required],
      endDate: ['', Validators.required]
    });

    this.fiscalPeriodForm = this.fb.group({
      fiscalYearId: [null as number | null, Validators.required],
      periodName: ['', Validators.required],
      startDate: ['', Validators.required],
      endDate: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.fiscalYearForm.valueChanges.pipe(takeUntil(this.destroy$)).subscribe(() => this.cdr.markForCheck());
    this.fiscalPeriodForm.valueChanges.pipe(takeUntil(this.destroy$)).subscribe(() => this.cdr.markForCheck());
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  ngOnChanges(): void {
    if (this.settings) {
      this.selectedAllowedCurrencies = String(this.settings.allowedCurrencies || '')
        .split(',')
        .map(c => c.trim())
        .filter(c => !!c);
      
      this.accountingForm.patchValue({
        accountingMethod: this.settings.accountingMethod,
        baseCurrency: this.settings.baseCurrency
      });

      const openYear = this.settings.fiscalYears?.find(y => y.open);
      if (openYear) {
        this.fiscalPeriodForm.patchValue({ fiscalYearId: openYear.id });
      }
    }
  }

  isCurrencySelected(code: string): boolean {
    return this.selectedAllowedCurrencies.includes(code);
  }

  toggleCurrency(code: string, checked: boolean): void {
    if (checked) {
      if (!this.selectedAllowedCurrencies.includes(code)) {
        this.selectedAllowedCurrencies = [...this.selectedAllowedCurrencies, code];
      }
    } else {
      this.selectedAllowedCurrencies = this.selectedAllowedCurrencies.filter(c => c !== code);
    }
  }

  onSaveAccounting(): void {
    this.saveSettings.emit({
      ...this.accountingForm.value,
      allowedCurrencies: this.selectedAllowedCurrencies.join(',')
    });
  }
}
