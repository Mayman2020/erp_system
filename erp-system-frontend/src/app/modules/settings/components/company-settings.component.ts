import { ChangeDetectionStrategy, ChangeDetectorRef, Component, EventEmitter, Input, OnChanges, Output } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { CompanySettings } from '../../../core/models/company-settings.models';

@Component({
  standalone: false,
  selector: 'app-company-settings',
  templateUrl: './company-settings.component.html',
  styleUrls: ['./company-settings.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CompanySettingsComponent implements OnChanges {
  @Input() settings: CompanySettings | null = null;
  @Input() canEdit = false;
  @Input() saving = false;
  @Input() errorKey = '';
  @Input() successKey = '';

  @Output() saveSettings = new EventEmitter<CompanySettings>();

  readonly monthOptions = [
    { value: 1, labelKey: 'MONTH.JANUARY' }, { value: 2, labelKey: 'MONTH.FEBRUARY' }, { value: 3, labelKey: 'MONTH.MARCH' },
    { value: 4, labelKey: 'MONTH.APRIL' }, { value: 5, labelKey: 'MONTH.MAY' }, { value: 6, labelKey: 'MONTH.JUNE' },
    { value: 7, labelKey: 'MONTH.JULY' }, { value: 8, labelKey: 'MONTH.AUGUST' }, { value: 9, labelKey: 'MONTH.SEPTEMBER' },
    { value: 10, labelKey: 'MONTH.OCTOBER' }, { value: 11, labelKey: 'MONTH.NOVEMBER' }, { value: 12, labelKey: 'MONTH.DECEMBER' }
  ];

  form: FormGroup;
  logoPreview = '';

  constructor(private fb: FormBuilder, private cdr: ChangeDetectorRef) {
    this.form = this.fb.group({
      companyNameEn: ['', [Validators.required, Validators.maxLength(200)]],
      companyNameAr: ['', [Validators.required, Validators.maxLength(200)]],
      taxId: ['', Validators.maxLength(60)],
      logoBase64: [''],
      fiscalYearStartMonth: [1, [Validators.required, Validators.min(1), Validators.max(12)]]
    });
  }

  ngOnChanges(): void {
    if (this.settings) {
      this.logoPreview = this.settings.logoBase64 || '';
      this.form.patchValue({
        companyNameEn: this.settings.companyNameEn || '',
        companyNameAr: this.settings.companyNameAr || '',
        taxId: this.settings.taxId || '',
        logoBase64: this.settings.logoBase64 || '',
        fiscalYearStartMonth: this.settings.fiscalYearStartMonth || 1
      });
    }
    if (!this.canEdit) {
      this.form.disable({ emitEvent: false });
    } else {
      this.form.enable({ emitEvent: false });
    }
  }

  onSelectLogo(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onload = () => {
      const data = (reader.result || '').toString();
      this.form.patchValue({ logoBase64: data });
      this.logoPreview = data;
      this.cdr.markForCheck();
    };
    reader.readAsDataURL(file);
  }

  onSave(): void {
    if (this.form.invalid || this.saving || !this.canEdit) {
      this.form.markAllAsTouched();
      return;
    }
    this.saveSettings.emit(this.form.getRawValue());
  }
}
