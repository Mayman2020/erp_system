import { Component, Input, OnChanges, OnDestroy, OnInit, SimpleChanges } from '@angular/core';
import { AbstractControl } from '@angular/forms';
import { Subscription } from 'rxjs';
import { TranslationService } from '../../../core/i18n/translation.service';
import { DateFormatService } from '../../../core/services/date-format.service';

@Component({ standalone: false,
  selector: 'app-form-field',
  template: `
    <div
      class="erp-form-field"
      [class.erp-form-field--invalid]="showError"
      [class.erp-form-field--disabled]="isDisabled"
      [class.erp-form-field--has-value]="hasValue"
    >
      <label class="erp-form-field__label" [for]="inputId">{{ labelKey | translate }}</label>

      <div class="erp-form-field__control">
        <mat-icon class="erp-form-field__icon" *ngIf="icon" aria-hidden="true">{{ icon }}</mat-icon>

        <ng-container [ngSwitch]="type">
          <textarea
            *ngSwitchCase="'textarea'"
            class="erp-input"
            [id]="inputId"
            [rows]="rows"
            [formControl]="control"
            [readonly]="readonly"
          ></textarea>

          <select
            *ngSwitchCase="'select'"
            class="erp-input erp-input--select"
            [id]="inputId"
            [formControl]="control"
            [disabled]="isDisabled"
          >
            <option *ngIf="includeEmptyOption" [ngValue]="emptyValue">{{ emptyLabelKey | translate }}</option>
            <option *ngFor="let option of options" [ngValue]="optionValue(option)">{{ optionLabel(option) }}</option>
          </select>

          <input
            *ngSwitchCase="'date'"
            class="erp-input"
            [id]="inputId"
            type="text"
            [value]="dateDisplayValue"
            [readonly]="readonly"
            [disabled]="isDisabled"
            [attr.placeholder]="datePlaceholder"
            inputmode="numeric"
            maxlength="10"
            (input)="onDateInput($event)"
            (blur)="onDateBlur()"
          />

          <input
            *ngSwitchDefault
            class="erp-input"
            [id]="inputId"
            [type]="type"
            [formControl]="control"
            [readonly]="readonly"
            [attr.min]="min !== null ? min : null"
            [attr.max]="max !== null ? max : null"
            [attr.step]="step || null"
          />
        </ng-container>
      </div>

      <div class="erp-form-field__message" *ngIf="hintKey && !showError">{{ hintKey | translate }}</div>
      <div class="erp-form-field__error" *ngIf="showError">{{ currentErrorKey | translate }}</div>
    </div>
  `
})
export class FormFieldComponent implements OnInit, OnChanges, OnDestroy {
  @Input() control: AbstractControl | null = null;
  @Input() labelKey = '';
  @Input() icon = '';
  @Input() type: 'text' | 'email' | 'password' | 'number' | 'date' | 'textarea' | 'select' = 'text';
  @Input() readonly = false;
  @Input() disabled = false;
  @Input() rows = 4;
  @Input() hintKey = '';
  @Input() step = '';
  @Input() min: number | null = null;
  @Input() max: number | null = null;
  @Input() options: any[] = [];
  @Input() optionValueField = 'code';
  @Input() optionLabelField = 'name';
  @Input() optionLabelPrefix = '';
  @Input() includeEmptyOption = false;
  @Input() emptyLabelKey = 'COMMON.ALL';
  @Input() emptyValue: any = '';
  @Input() inputId = `erp-field-${Math.random().toString(36).slice(2, 10)}`;

  dateDisplayValue = '';
  readonly datePlaceholder = this.dateFormatService.placeholder;
  private controlSubscription?: Subscription;

  constructor(
    private translationService: TranslationService,
    private dateFormatService: DateFormatService
  ) {}

  ngOnInit(): void {
    this.bindDateControl();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['control'] || changes['type']) {
      this.bindDateControl();
    }
  }

  ngOnDestroy(): void {
    this.controlSubscription?.unsubscribe();
  }

  get showError(): boolean {
    return !!this.control && this.control.invalid && (this.control.touched || this.control.dirty);
  }

  get hasValue(): boolean {
    if (this.type === 'date') {
      return !!this.dateDisplayValue;
    }
    if (!this.control) {
      return false;
    }
    const value = this.control.value;
    return value !== null && value !== undefined && value !== '';
  }

  get isDisabled(): boolean {
    return this.disabled || !!this.control?.disabled;
  }

  get currentErrorKey(): string {
    if (!this.control || !this.control.errors) {
      return '';
    }
    if (this.control.errors['required']) {
      return 'VALIDATION.REQUIRED';
    }
    if (this.control.errors['email']) {
      return 'VALIDATION.EMAIL_INVALID';
    }
    if (this.control.errors['invalidDate']) {
      return 'VALIDATION.INVALID_DATE';
    }
    if (this.control.errors['min']) {
      return 'VALIDATION.AMOUNT_GT_ZERO';
    }
    if (this.control.errors['maxlength'] || this.control.errors['minlength']) {
      return 'VALIDATION.INVALID_LENGTH';
    }
    return 'VALIDATION.FAILED';
  }

  optionValue(option: any): any {
    if (option && typeof option === 'object') {
      return option[this.optionValueField] !== undefined ? option[this.optionValueField] : option['value'];
    }
    return option;
  }

  optionLabel(option: any): string {
    if (option && typeof option === 'object') {
      if (option['labelKey']) {
        return this.translationService.instant(option['labelKey']);
      }
      const raw = option[this.optionLabelField] !== undefined ? option[this.optionLabelField] : option['label'];
      if (this.optionLabelPrefix) {
        return this.translationService.instant(`${this.optionLabelPrefix}${raw}`);
      }
      return raw;
    }
    if (this.optionLabelPrefix) {
      return this.translationService.instant(`${this.optionLabelPrefix}${option}`);
    }
    return `${option}`;
  }

  onDateInput(event: Event): void {
    const rawValue = (event.target as HTMLInputElement).value || '';
    this.dateDisplayValue = this.dateFormatService.normalizeDisplayInput(rawValue);

    if (!this.control) {
      return;
    }

    const isoValue = this.dateFormatService.toIsoDate(this.dateDisplayValue);
    if (isoValue) {
      this.control.setValue(isoValue, { emitEvent: false });
      this.clearInvalidDateError();
      return;
    }

    if (!this.dateDisplayValue) {
      this.control.setValue('', { emitEvent: false });
      this.clearInvalidDateError();
      return;
    }

    this.control.setValue('', { emitEvent: false });
  }

  onDateBlur(): void {
    if (!this.control) {
      return;
    }

    this.control.markAsTouched();

    if (!this.dateDisplayValue) {
      this.clearInvalidDateError();
      return;
    }

    const isoValue = this.dateFormatService.toIsoDate(this.dateDisplayValue);
    if (isoValue) {
      this.dateDisplayValue = this.dateFormatService.format(isoValue);
      this.control.setValue(isoValue, { emitEvent: false });
      this.clearInvalidDateError();
      return;
    }

    this.applyInvalidDateError();
  }

  private bindDateControl(): void {
    this.controlSubscription?.unsubscribe();

    if (this.type !== 'date' || !this.control) {
      this.dateDisplayValue = '';
      return;
    }

    this.syncDateDisplayFromControl(this.control.value);
    this.controlSubscription = this.control.valueChanges.subscribe((value) => this.syncDateDisplayFromControl(value));
  }

  private syncDateDisplayFromControl(value: unknown): void {
    if (this.type !== 'date') {
      return;
    }
    this.dateDisplayValue = value ? this.dateFormatService.format(value) : '';
  }

  private applyInvalidDateError(): void {
    if (!this.control) {
      return;
    }
    this.control.setErrors({ ...(this.control.errors || {}), invalidDate: true });
  }

  private clearInvalidDateError(): void {
    if (!this.control?.errors?.['invalidDate']) {
      return;
    }
    const { invalidDate, ...rest } = this.control.errors || {};
    this.control.setErrors(Object.keys(rest).length ? rest : null);
  }
}
