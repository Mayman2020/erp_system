import { ChangeDetectionStrategy, ChangeDetectorRef, Component, EventEmitter, Input, Output } from '@angular/core';
import { FormArray, FormBuilder, FormGroup } from '@angular/forms';
import { ProductDto } from '../../../core/models/erp.models';
import { calcLineTotal, calcSimpleLineTotal, createFullLineGroup, createSimpleLineGroup } from '../../utils/erp-document.utils';

@Component({
  standalone: false,
  selector: 'app-document-lines-table',
  templateUrl: './document-lines-table.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DocumentLinesTableComponent {
  @Input({ required: true }) parentForm!: FormGroup;
  @Input({ required: true }) products: ProductDto[] = [];
  @Input() readOnly = false;
  @Input() simple = false;
  @Input() priceField: 'salePrice' | 'costPrice' = 'salePrice';
  @Output() linesChanged = new EventEmitter<void>();

  constructor(private fb: FormBuilder, private cdr: ChangeDetectorRef) {}

  get lines(): FormArray {
    return this.parentForm.get('lines') as FormArray;
  }

  addLine(): void {
    this.lines.push(this.simple ? createSimpleLineGroup(this.fb) : createFullLineGroup(this.fb));
    this.linesChanged.emit();
    this.cdr.markForCheck();
  }

  removeLine(index: number): void {
    if (this.lines.length <= 1) {
      return;
    }
    this.lines.removeAt(index);
    this.linesChanged.emit();
    this.cdr.markForCheck();
  }

  onProductChange(index: number): void {
    const row = this.lines.at(index);
    if (!row) {
      return;
    }
    const productId = Number(row.get('productId')?.value);
    const product = this.products.find((p) => p.id === productId);
    if (!product) {
      this.linesChanged.emit();
      return;
    }
    const unitPrice = this.priceField === 'salePrice'
      ? (product.salePrice ?? product.costPrice ?? 0)
      : (product.costPrice ?? product.salePrice ?? 0);
    if (this.simple) {
      row.patchValue({ unitPrice });
    } else {
      row.patchValue({ unitPrice, description: product.name || product.nameEn || '' });
    }
    this.linesChanged.emit();
  }

  lineTotal(index: number): number {
    const row = this.lines.at(index);
    if (!row) {
      return 0;
    }
    const qty = Number(row.get('quantity')?.value || 0);
    const price = Number(row.get('unitPrice')?.value || 0);
    if (this.simple) {
      return calcSimpleLineTotal(qty, price);
    }
    return calcLineTotal(qty, price, Number(row.get('discountPercent')?.value || 0), Number(row.get('taxPercent')?.value || 0));
  }
}
