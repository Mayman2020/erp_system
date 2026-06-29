import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ProductDto } from '../../core/models/erp.models';

export interface DocumentLineValues {
  productId: number;
  description?: string;
  quantity: number;
  unitPrice: number;
  discountPercent?: number;
  taxPercent?: number;
}

export function calcLineTotal(
  quantity: number,
  unitPrice: number,
  discountPercent = 0,
  taxPercent = 0
): number {
  const gross = quantity * unitPrice;
  const net = gross - gross * (discountPercent / 100);
  return net + net * (taxPercent / 100);
}

export function calcSimpleLineTotal(quantity: number, unitPrice: number): number {
  return quantity * unitPrice;
}

export function calcDocumentTotals(
  lines: Array<{ quantity: number; unitPrice: number; discountPercent?: number; taxPercent?: number }>,
  headerDiscount = 0
): { subtotal: number; tax: number; total: number } {
  let subtotal = 0;
  let tax = 0;
  for (const line of lines) {
    const qty = Number(line.quantity || 0);
    const price = Number(line.unitPrice || 0);
    const discount = Number(line.discountPercent || 0);
    const taxPct = Number(line.taxPercent || 0);
    const gross = qty * price;
    const net = gross - gross * (discount / 100);
    subtotal += net;
    tax += net * (taxPct / 100);
  }
  const discount = Number(headerDiscount || 0);
  const adjustedSubtotal = Math.max(0, subtotal - discount);
  return { subtotal: adjustedSubtotal, tax, total: adjustedSubtotal + tax };
}

export function calcReturnTotals(
  lines: Array<{ quantity: number; unitPrice: number }>,
  headerTax = 0
): { subtotal: number; tax: number; total: number } {
  const subtotal = lines.reduce((sum, line) => sum + calcSimpleLineTotal(Number(line.quantity || 0), Number(line.unitPrice || 0)), 0);
  const tax = Number(headerTax || 0);
  return { subtotal, tax, total: subtotal + tax };
}

export function createFullLineGroup(fb: FormBuilder): FormGroup {
  return fb.group({
    productId: [null, Validators.required],
    description: [''],
    quantity: [1, [Validators.required, Validators.min(0.0001)]],
    unitPrice: [0, [Validators.required, Validators.min(0)]],
    discountPercent: [0],
    taxPercent: [0]
  });
}

export function createSimpleLineGroup(fb: FormBuilder): FormGroup {
  return fb.group({
    productId: [null, Validators.required],
    quantity: [1, [Validators.required, Validators.min(0.0001)]],
    unitPrice: [0, [Validators.required, Validators.min(0)]]
  });
}

export function patchFullLines(fb: FormBuilder, linesArray: FormArray, lines: DocumentLineValues[]): void {
  linesArray.clear();
  lines.forEach((line) => {
    linesArray.push(fb.group({
      productId: [line.productId, Validators.required],
      description: [line.description || ''],
      quantity: [line.quantity, [Validators.required, Validators.min(0.0001)]],
      unitPrice: [line.unitPrice, [Validators.required, Validators.min(0)]],
      discountPercent: [line.discountPercent || 0],
      taxPercent: [line.taxPercent || 0]
    }));
  });
}

export function patchSimpleLines(
  fb: FormBuilder,
  linesArray: FormArray,
  lines: Array<{ productId: number; quantity: number; unitPrice: number }>
): void {
  linesArray.clear();
  lines.forEach((line) => {
    linesArray.push(fb.group({
      productId: [line.productId, Validators.required],
      quantity: [line.quantity, [Validators.required, Validators.min(0.0001)]],
      unitPrice: [line.unitPrice, [Validators.required, Validators.min(0)]]
    }));
  });
}

export function applyProductPrice(
  products: ProductDto[],
  productId: number,
  priceField: 'salePrice' | 'costPrice'
): { unitPrice: number; description: string } {
  const product = products.find((p) => p.id === productId);
  if (!product) {
    return { unitPrice: 0, description: '' };
  }
  const unitPrice = priceField === 'salePrice'
    ? (product.salePrice ?? product.costPrice ?? 0)
    : (product.costPrice ?? product.salePrice ?? 0);
  return { unitPrice, description: product.name || product.nameEn || '' };
}

export function mapAmountRow(row: Record<string, unknown>): Record<string, unknown> {
  const mapped = { ...row };
  ['totalAmount', 'subtotal', 'taxAmount', 'discountAmount', 'remainingAmount', 'paidAmount', 'quantity', 'producedQuantity'].forEach((key) => {
    const value = mapped[key];
    if (value !== undefined && value !== null && typeof value === 'number') {
      mapped[key] = Number(value).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    }
  });
  return mapped;
}

export const DOCUMENT_ACTIONS = [
  { id: 'view', labelKey: 'COMMON.VIEW', className: 'erp-action-secondary' },
  { id: 'edit', labelKey: 'COMMON.EDIT', className: 'erp-action-info', disabledWhen: (row: Record<string, unknown>) => String(row['status']) !== 'DRAFT' },
  { id: 'approve', labelKey: 'COMMON.APPROVE', className: 'erp-action-success', disabledWhen: (row: Record<string, unknown>) => String(row['status']) !== 'DRAFT' },
  { id: 'cancel', labelKey: 'COMMON.CANCEL', className: 'erp-action-warning', disabledWhen: (row: Record<string, unknown>) => String(row['status']) !== 'APPROVED' },
  { id: 'delete', labelKey: 'COMMON.DELETE', className: 'erp-action-danger', disabledWhen: (row: Record<string, unknown>) => String(row['status']) !== 'DRAFT' }
];
