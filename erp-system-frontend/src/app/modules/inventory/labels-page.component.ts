import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { LabelPreviewDto, ProductDto } from '../../core/models/erp.models';
import { ErpApiService } from '../../core/services/erp-api.service';

@Component({
  standalone: false,
  selector: 'app-labels-page',
  templateUrl: './labels-page.component.html',
  styleUrls: ['./labels-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class LabelsPageComponent implements OnInit {
  readonly titleKey = 'MENU.BARCODE_LABELS';
  products: ProductDto[] = [];
  preview: LabelPreviewDto | null = null;
  loading = false;
  errorKey = '';

  readonly form = this.fb.group({
    productId: [null as number | null, Validators.required]
  });

  constructor(private api: ErpApiService, private fb: FormBuilder, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.api.getProducts().subscribe({
      next: (products) => {
        this.products = products || [];
        this.cdr.markForCheck();
      }
    });
  }

  get productOptions() {
    return [{ id: null, label: '—' }, ...this.products.map((p) => ({ id: p.id, label: `${p.code} - ${p.name || p.nameEn}` }))];
  }

  previewLabel(): void {
    const productId = Number(this.form.getRawValue().productId);
    if (!productId) return;
    this.loading = true;
    this.errorKey = '';
    this.api.getLabelPreview(productId).subscribe({
      next: (preview) => {
        this.preview = preview;
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.errorKey = 'COMMON.ERROR_LOADING';
        this.loading = false;
        this.cdr.markForCheck();
      }
    });
  }
}
