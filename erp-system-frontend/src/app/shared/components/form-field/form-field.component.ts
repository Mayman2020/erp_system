import { Component, ElementRef, Input, ViewChild } from '@angular/core';
import { AbstractControl } from '@angular/forms';
import { TranslationService } from '../../../core/i18n/translation.service';

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
        <mat-icon class="erp-form-field__icon" *ngIf="icon && type !== 'date'" aria-hidden="true">{{ icon }}</mat-icon>

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

          <ng-container *ngSwitchCase="'date'">
            <button
              type="button"
              class="erp-form-field__date-opener"
              (click)="openDatePicker($event)"
              [disabled]="isDisabled"
              [attr.aria-label]="labelKey | translate"
            >
              <mat-icon aria-hidden="true">{{ dateIconGlyph }}</mat-icon>
            </button>
            <input
              #dateInputRef
              class="erp-input erp-input--date"
              [id]="inputId"
              type="date"
              [formControl]="control!"
              [readonly]="readonly"
            />
          </ng-container>

          <input
            *ngSwitchDefault
            class="erp-input"
            [id]="inputId"
            [type]="type"
            [formControl]="control"
            [readonly]="readonly"
            [attr.placeholder]="placeholderKey ? (placeholderKey | translate) : null"
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
export class FormFieldComponent {
  @ViewChild('dateInputRef') private dateInputRef?: ElementRef<HTMLInputElement>;

  @Input() control: AbstractControl | null = null;
  @Input() labelKey = '';
  @Input() icon = '';
  @Input() type: 'text' | 'email' | 'password' | 'number' | 'date' | 'textarea' | 'select' = 'text';
  @Input() readonly = false;
  @Input() disabled = false;
  @Input() rows = 4;
  @Input() hintKey = '';
  @Input() placeholderKey = '';
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

  constructor(private translationService: TranslationService) {}

  get dateIconGlyph(): string {
    return (this.icon || 'calendar_today').trim() || 'calendar_today';
  }

  openDatePicker(event: Event): void {
    event.preventDefault();
    event.stopPropagation();
    if (this.isDisabled) {
      return;
    }
    const el = this.dateInputRef?.nativeElement;
    if (!el) {
      return;
    }
    const anyInput = el as HTMLInputElement & { showPicker?: () => void | Promise<void> };
    if (typeof anyInput.showPicker === 'function') {
      try {
        const maybePromise = anyInput.showPicker() as void | Promise<void> | undefined;
        if (maybePromise && typeof (maybePromise as Promise<void>).then === 'function') {
          (maybePromise as Promise<void>).catch(() => this.focusDateInput(el));
        }
      } catch {
        this.focusDateInput(el);
      }
      return;
    }
    this.focusDateInput(el);
  }

  private focusDateInput(el: HTMLInputElement): void {
    el.focus();
    try {
      el.click();
    } catch {
      /* Safari / older browsers */
    }
  }

  get showError(): boolean {
    return !!this.control && this.control.invalid && (this.control.touched || this.control.dirty);
  }

  get hasValue(): boolean {
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
      const localizedLabel = this.resolveLocalizedOptionLabel(option);
      if (localizedLabel) {
        return localizedLabel;
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

  private resolveLocalizedOptionLabel(option: Record<string, unknown>): string {
    const language = this.translationService.currentLanguage === 'ar' ? 'Ar' : 'En';
    const alternate = language === 'Ar' ? 'En' : 'Ar';
    const normalizedField = this.optionLabelField.replace(/(Ar|En)$/i, '');
    const preferredField = `${normalizedField}${language}`;
    const alternateField = `${normalizedField}${alternate}`;
    const preferred = option[preferredField];
    const fallback = option[alternateField];
    if (typeof preferred === 'string' && preferred.trim()) {
      return preferred;
    }
    if (typeof fallback === 'string' && fallback.trim()) {
      return fallback;
    }
    return '';
  }
}
