import { Component, Input } from '@angular/core';
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
      [ngClass]="extraClass"
    >
      <label class="erp-form-field__label" [for]="inputId" *ngIf="type !== 'date'">{{ labelKey | translate }}</label>

      <div class="erp-form-field__control" [class.erp-form-field__control--password]="type === 'password'">
        <mat-icon class="erp-form-field__icon" *ngIf="icon && type !== 'date' && type !== 'password'" aria-hidden="true">{{ icon }}</mat-icon>

        <ng-container [ngSwitch]="type">
          <textarea
            *ngSwitchCase="'textarea'"
            class="erp-input"
            [ngClass]="controlClass"
            [id]="inputId"
            [rows]="rows"
            [formControl]="control"
            [readonly]="readonly"
          ></textarea>

          <select
            *ngSwitchCase="'select'"
            class="erp-input erp-input--select"
            [ngClass]="controlClass"
            [id]="inputId"
            [formControl]="control"
            [disabled]="isDisabled"
          >
            <option *ngIf="includeEmptyOption" [ngValue]="emptyValue">{{ emptyLabelKey | translate }}</option>
            <option *ngFor="let option of options" [ngValue]="optionValue(option)">{{ optionLabel(option) }}</option>
          </select>

          <app-date-field
            *ngSwitchCase="'date'"
            [formControl]="$any(control)"
            [labelKey]="labelKey"
            [compact]="true"
          ></app-date-field>

          <div *ngSwitchCase="'password'" class="erp-form-field__password">
            <mat-icon class="erp-form-field__icon erp-form-field__icon--password" *ngIf="icon" aria-hidden="true">{{ icon }}</mat-icon>
            <input
              class="erp-input"
              [ngClass]="controlClass"
              [id]="inputId"
              [type]="passwordHidden ? 'password' : 'text'"
              [formControl]="control"
              [readonly]="readonly"
              [attr.placeholder]="placeholderKey ? (placeholderKey | translate) : null"
            />
            <button
              *ngIf="passwordToggle"
              type="button"
              class="erp-form-field__password-toggle"
              (click)="passwordHidden = !passwordHidden"
              [attr.aria-label]="(passwordHidden ? 'AUTH.SHOW_PASSWORD' : 'AUTH.HIDE_PASSWORD') | translate"
            >
              <mat-icon aria-hidden="true">{{ passwordHidden ? 'visibility' : 'visibility_off' }}</mat-icon>
            </button>
          </div>

          <input
            *ngSwitchDefault
            class="erp-input"
            [ngClass]="controlClass"
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
  @Input() extraClass = '';
  @Input() controlClass = '';
  @Input() passwordToggle = false;
  @Input() inputId = `erp-field-${Math.random().toString(36).slice(2, 10)}`;

  passwordHidden = true;

  constructor(private translationService: TranslationService) {}

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
