import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { LicenseDto } from '../core/models/erp.models';
import { ErpApiService } from '../core/services/erp-api.service';

@Component({
  standalone: false,
  selector: 'app-license-page',
  templateUrl: './license-page.component.html',
  styleUrls: ['./license-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class LicensePageComponent implements OnInit {
  current: LicenseDto | null = null;
  saving = false;
  readonly form = this.fb.group({
    licenseKey: ['', Validators.required],
    customerName: ['', Validators.required],
    modulesCsv: [''],
    maxUsers: [10, Validators.required],
    validFrom: ['', Validators.required],
    validTo: ['', Validators.required],
    graceDays: [7],
    signature: ['', Validators.required]
  });

  constructor(private api: ErpApiService, private fb: FormBuilder, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void { this.load(); }

  activate(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.saving = true;
    this.cdr.markForCheck();
    this.api.activateLicense(this.form.getRawValue() as any).subscribe({
      next: (license) => { this.current = license; this.saving = false; this.cdr.markForCheck(); },
      error: () => { this.saving = false; this.cdr.markForCheck(); }
    });
  }

  private load(): void {
    this.api.getCurrentLicense().subscribe({ next: (license) => { this.current = license; this.cdr.markForCheck(); } });
  }
}
