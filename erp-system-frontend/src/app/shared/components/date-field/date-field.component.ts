import { Component, Input, OnDestroy, OnInit, Pipe, PipeTransform, forwardRef } from '@angular/core';
import { ControlValueAccessor, FormControl, NG_VALUE_ACCESSOR, ReactiveFormsModule } from '@angular/forms';
import { NgIf } from '@angular/common';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { Subscription } from 'rxjs';
import { TranslationService } from '../../../core/i18n/translation.service';
import { formatApiDate, parseApiDate } from '../../../core/utils/date-value.utils';

@Pipe({
  name: 'translate',
  standalone: true,
  pure: false
})
class StandaloneTranslatePipe implements PipeTransform {
  constructor(private readonly translationService: TranslationService) {}

  transform(key: string): string {
    return key ? this.translationService.instant(key) : '';
  }
}

@Component({
  selector: 'app-date-field',
  standalone: true,
  imports: [NgIf, ReactiveFormsModule, MatFormFieldModule, MatInputModule, MatDatepickerModule, MatNativeDateModule, StandaloneTranslatePipe],
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => DateFieldComponent),
    multi: true
  }],
  template: `
    <mat-form-field appearance="outline"
      class="app-date-field"
      [class.app-date-field--compact]="compact"
      [class.app-date-field--filter]="filter"
      subscriptSizing="dynamic">
      <mat-label *ngIf="labelKey">{{ labelKey | translate }}</mat-label>
      <mat-label *ngIf="!labelKey && label">{{ label }}</mat-label>
      <input matInput
        [matDatepicker]="picker"
        [formControl]="pickerCtrl"
        [placeholder]="placeholder"
        [min]="minDate"
        [max]="maxDate"
        (blur)="onTouched()" />
      <mat-datepicker-toggle matIconSuffix [for]="picker"></mat-datepicker-toggle>
      <mat-datepicker #picker></mat-datepicker>
      <mat-error *ngIf="pickerCtrl.hasError('matDatepickerParse')">{{ invalidDateKey | translate }}</mat-error>
      <mat-error *ngIf="required && pickerCtrl.hasError('required')">{{ requiredKey | translate }}</mat-error>
    </mat-form-field>
  `,
  styles: [`
    :host { display: block; width: 100%; }
    .app-date-field { width: 100%; }
    .app-date-field--filter { max-width: 170px; min-width: 140px; }
  `]
})
export class DateFieldComponent implements ControlValueAccessor, OnInit, OnDestroy {
  @Input() label = '';
  @Input() labelKey = '';
  @Input() placeholder = 'DD/MM/YYYY';
  @Input() compact = false;
  @Input() filter = false;
  @Input() required = false;
  @Input() min: string | Date | null = null;
  @Input() max: string | Date | null = null;
  @Input() invalidDateKey = 'VALIDATION.INVALID_DATE';
  @Input() requiredKey = 'COMMON.REQUIRED';

  pickerCtrl = new FormControl<Date | null>(null);
  private sub?: Subscription;
  private onChange: (v: string | null) => void = () => {};
  private onTouchedCb: () => void = () => {};

  get minDate(): Date | null { return parseApiDate(this.min); }
  get maxDate(): Date | null { return parseApiDate(this.max); }

  ngOnInit(): void {
    this.sub = this.pickerCtrl.valueChanges.subscribe((v) => this.onChange(v ? formatApiDate(v) : null));
  }

  ngOnDestroy(): void { this.sub?.unsubscribe(); }

  writeValue(value: string | Date | null): void {
    this.pickerCtrl.setValue(parseApiDate(value), { emitEvent: false });
  }

  registerOnChange(fn: (v: string | null) => void): void { this.onChange = fn; }
  registerOnTouched(fn: () => void): void { this.onTouchedCb = fn; }
  onTouched(): void { this.onTouchedCb(); }

  setDisabledState(disabled: boolean): void {
    disabled ? this.pickerCtrl.disable({ emitEvent: false }) : this.pickerCtrl.enable({ emitEvent: false });
  }
}
