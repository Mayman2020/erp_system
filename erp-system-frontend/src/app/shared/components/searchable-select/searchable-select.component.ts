import {
  Component,
  EventEmitter,
  Input,
  Output,
  forwardRef,
  OnChanges,
  OnDestroy,
  SimpleChanges
} from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { TranslationService } from '../../../core/i18n/translation.service';
import { LovSelectDialogComponent } from '../lov-select-dialog/lov-select-dialog.component';

@Component({
  standalone: false,
  selector: 'app-searchable-select',
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => SearchableSelectComponent),
      multi: true
    }
  ],
  template: `
    <div
      class="erp-lov-field"
      [class.erp-lov-field--toolbar]="variant === 'toolbar'"
      [class.erp-lov-field--form]="variant === 'form'"
      [class.erp-lov-field--disabled]="disabled">
      <label class="erp-lov-label" *ngIf="label">{{ label | translate }}</label>

      <div class="erp-lov-control">
        <input
          type="text"
          class="erp-lov-input"
          [value]="displayText"
          readonly
          [disabled]="disabled"
          [placeholder]="isEmpty ? (placeholder | translate) : ''"
          (click)="openPicker($event)"
          (keydown.enter)="openPicker($event); $event.preventDefault()"
          (blur)="onBlur()">

        <button
          *ngIf="clearable && !isEmpty && !disabled"
          type="button"
          class="erp-lov-clear-btn"
          tabindex="-1"
          (mousedown)="$event.preventDefault()"
          (click)="clearSelection($event)"
          [attr.aria-label]="'LOV.CLEAR' | translate">
          <mat-icon aria-hidden="true">close</mat-icon>
        </button>

        <button
          type="button"
          class="erp-lov-search-btn"
          tabindex="-1"
          [disabled]="disabled"
          (mousedown)="$event.preventDefault()"
          (click)="openPicker($event)"
          [attr.aria-label]="'LOV.OPEN' | translate">
          <mat-icon aria-hidden="true">search</mat-icon>
        </button>
      </div>

      <div class="erp-lov-error" *ngIf="required && touched && isEmpty">
        {{ 'COMMON.REQUIRED' | translate }}
      </div>
    </div>
  `,
  styles: [`
    :host { display: block; width: 100%; }
    .erp-lov-field { position: relative; display: block; width: 100%; }
    .erp-lov-label { display: block; font-size: 0.78rem; font-weight: 600; color: var(--text-muted, var(--erp-text-soft)); margin-bottom: 6px; }
    .erp-lov-control {
      display: flex; align-items: stretch; min-height: 42px;
      border: 1px solid var(--border-color, var(--erp-border)); border-radius: var(--radius-md, 8px);
      background: var(--bg-muted, var(--erp-bg-muted)); overflow: hidden;
    }
    .erp-lov-control:focus-within {
      border-color: var(--color-primary, var(--erp-info));
      box-shadow: 0 0 0 2px color-mix(in srgb, var(--color-primary, var(--erp-info)) 16%, transparent);
    }
    .erp-lov-input {
      flex: 1; min-width: 0; border: none; background: transparent; outline: none;
      padding: 0 12px; color: var(--text-primary, var(--erp-text)); cursor: pointer;
    }
    .erp-lov-clear-btn, .erp-lov-search-btn {
      display: inline-flex; align-items: center; justify-content: center; border: none;
      border-inline-start: 1px solid var(--border-color, var(--erp-border)); background: transparent; cursor: pointer;
    }
    .erp-lov-clear-btn { width: 32px; color: var(--text-muted, var(--erp-text-soft)); }
    .erp-lov-search-btn { width: 42px; color: var(--color-primary, var(--erp-info)); background: color-mix(in srgb, var(--color-primary, var(--erp-info)) 8%, transparent); }
    .erp-lov-error { font-size: 0.72rem; color: var(--color-danger, var(--erp-danger)); margin-top: 4px; }
    .erp-lov-field--disabled .erp-lov-control { opacity: 0.72; }
  `]
})
export class SearchableSelectComponent implements ControlValueAccessor, OnChanges, OnDestroy {
  @Input() items: any[] = [];
  @Input() label = '';
  @Input() placeholder = 'LOV.SELECT_PLACEHOLDER';
  @Input() variant: 'form' | 'toolbar' = 'form';
  @Input() bindLabel: string | null = null;
  @Input() bindValue = 'id';
  @Input() required = false;
  @Input() clearable = true;
  @Input() serverSide = false;
  @Output() searchTextChange = new EventEmitter<string>();

  displayText = '';
  touched = false;
  disabled = false;

  private _value: unknown;
  private openDialogRef: ReturnType<MatDialog['open']> | null = null;

  onChange: (value: unknown) => void = () => {};
  onTouched: () => void = () => {};

  constructor(
    private readonly dialog: MatDialog,
    private readonly translationService: TranslationService
  ) {}

  get isEmpty(): boolean {
    return this._value === null || this._value === undefined || this._value === '';
  }

  ngOnDestroy(): void {
    this.openDialogRef?.close();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['items']) {
      this.syncDisplayFromValue();
      this.refreshOpenDialogItems();
    }
  }

  openPicker(event?: Event): void {
    event?.preventDefault();
    event?.stopPropagation();
    if (this.disabled) return;
    this.openLovDialog();
  }

  onBlur(): void {
    this.touched = true;
    this.onTouched();
  }

  clearSelection(event: Event): void {
    event.preventDefault();
    event.stopPropagation();
    if (this.disabled) return;
    this._value = null;
    this.displayText = '';
    this.touched = true;
    this.onChange(null);
    this.onTouched();
  }

  getItemLabel(item: any): string {
    if (!item) return '';
    if (this.bindLabel) return String(item[this.bindLabel] ?? '');
    const lang = this.translationService.currentLanguage;
    const name = lang === 'ar'
      ? (item.nameAr || item.label || item.fullName || item.name || '')
      : (item.nameEn || item.fullName || item.name || item.label || '');
    const code = (item.code || '').trim();
    if (code && name && !name.includes(code)) return `${code} — ${name}`;
    return code || name || String(item[this.bindValue] ?? '');
  }

  writeValue(value: unknown): void {
    this._value = value;
    this.syncDisplayFromValue();
  }

  registerOnChange(fn: (value: unknown) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.disabled = isDisabled;
  }

  private itemValue(item: any): unknown {
    return item?.[this.bindValue];
  }

  private onSelected(item: any): void {
    this._value = item ? this.itemValue(item) : null;
    this.displayText = item ? this.getItemLabel(item) : '';
    this.onChange(this._value);
  }

  private syncDisplayFromValue(): void {
    if (!this.isEmpty && this.items?.length) {
      const item = this.items.find((i) => String(this.itemValue(i)) === String(this._value));
      this.displayText = item ? this.getItemLabel(item) : this.displayText;
      return;
    }
    if (this.isEmpty) {
      this.displayText = '';
    }
  }

  private openLovDialog(): void {
    if (this.openDialogRef) return;

    this.openDialogRef = this.dialog.open(LovSelectDialogComponent, {
      width: '760px',
      maxWidth: '94vw',
      maxHeight: '88vh',
      autoFocus: true,
      data: {
        title: this.label,
        items: this.items,
        selectedValue: this._value,
        bindValue: this.bindValue,
        serverSide: this.serverSide,
        getItemLabel: (item: unknown) => this.getItemLabel(item),
        getItemValue: (item: unknown) => (item as Record<string, unknown>)?.[this.bindValue],
        onSearch: (query: string) => this.searchTextChange.emit(query)
      }
    });

    this.openDialogRef.afterClosed().subscribe((item) => {
      this.openDialogRef = null;
      if (item !== undefined) {
        this.onSelected(item);
      }
    });
  }

  private refreshOpenDialogItems(): void {
    const ref = this.openDialogRef;
    if (!ref) return;
    const component = ref.componentInstance as LovSelectDialogComponent | undefined;
    component?.updateItems(this.items);
  }
}
