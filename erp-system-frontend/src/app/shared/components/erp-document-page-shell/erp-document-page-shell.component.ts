import { ChangeDetectionStrategy, ChangeDetectorRef, Component, EventEmitter, Input, Output } from '@angular/core';
import { AbstractControl, FormGroup } from '@angular/forms';
import { ProductDto, WarehouseDto } from '../../../core/models/erp.models';
import { DataTableAction, DataTableColumn } from '../data-table/data-table.component';

@Component({
  standalone: false,
  selector: 'app-erp-document-page-shell',
  templateUrl: './erp-document-page-shell.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ErpDocumentPageShellComponent {
  @Input({ required: true }) titleKey!: string;
  @Input({ required: true }) columns: DataTableColumn[] = [];
  @Input({ required: true }) rows: Array<Record<string, unknown>> = [];
  @Input({ required: true }) form!: FormGroup;
  @Input() actions: DataTableAction[] = [];
  @Input() statusOptions: string[] = ['DRAFT', 'APPROVED', 'CANCELLED'];
  @Input() loading = false;
  @Input() saving = false;
  @Input() errorKey = '';
  @Input() successKey = '';
  @Input() formVisible = false;
  @Input() formMode: 'create' | 'edit' | 'view' = 'create';
  @Input() selectedId: number | null = null;
  @Input() auditRecord: Record<string, unknown> | null = null;
  @Input() parties: Array<{ id: number; label: string }> = [];
  @Input() products: ProductDto[] = [];
  @Input() warehouses: WarehouseDto[] = [];
  @Input() numberField = 'documentNumber';
  @Input() dateField = 'documentDate';
  @Input() partyField = 'partyId';
  @Input() numberLabelKey = 'ERP.NUMBER';
  @Input() dateLabelKey = 'ERP.DATE';
  @Input() partyLabelKey = 'INVOICES.CUSTOMER';
  @Input() priceField: 'salePrice' | 'costPrice' = 'salePrice';
  @Input() showWarehouse = false;
  @Input() showValidUntil = false;
  @Input() showDueDate = false;
  @Input() dueDateField = 'dueDate';
  @Input() dueDateLabelKey = 'INVOICES.DUE_DATE';
  @Input() showDiscount = true;
  @Input() showHeaderTax = false;
  @Input() showInvoiceLink = false;
  @Input() isReturnMode = false;
  @Input() totals: { subtotal: number; tax: number; total: number } = { subtotal: 0, tax: 0, total: 0 };

  @Output() search = new EventEmitter<Record<string, string>>();
  @Output() create = new EventEmitter<void>();
  @Output() closeForm = new EventEmitter<void>();
  @Output() save = new EventEmitter<void>();
  @Output() approve = new EventEmitter<void>();
  @Output() tableAction = new EventEmitter<{ actionId: string; row: Record<string, unknown> }>();
  @Output() linesChanged = new EventEmitter<void>();
  @Output() clearError = new EventEmitter<void>();
  @Output() clearSuccess = new EventEmitter<void>();

  constructor(public cdr: ChangeDetectorRef) {}

  get readOnly(): boolean {
    return this.formMode === 'view';
  }

  get numberControl(): AbstractControl | null {
    return this.form?.get(this.numberField) ?? null;
  }

  get dateControl(): AbstractControl | null {
    return this.form?.get(this.dateField) ?? null;
  }

  get partyControl(): AbstractControl | null {
    return this.form?.get(this.partyField) ?? null;
  }

  get validUntilControl(): AbstractControl | null {
    return this.form?.get('validUntil') ?? null;
  }

  get dueDateControl(): AbstractControl | null {
    return this.form?.get(this.dueDateField) ?? null;
  }

  get warehouseControl(): AbstractControl | null {
    return this.form?.get('warehouseId') ?? null;
  }

  get invoiceIdControl(): AbstractControl | null {
    return this.form?.get('invoiceId') ?? null;
  }

  get discountControl(): AbstractControl | null {
    return this.form?.get('discountAmount') ?? null;
  }

  get taxControl(): AbstractControl | null {
    return this.form?.get('taxAmount') ?? null;
  }

  get notesControl(): AbstractControl | null {
    return this.form?.get('notes') ?? null;
  }

  get warehouseOptions(): Array<{ id: number | null; label: string }> {
    const items = (this.warehouses || []).map((w) => ({
      id: w.id,
      label: `${w.code} - ${w.nameEn || w.name || ''}`
    }));
    return [{ id: null, label: '—' }, ...items];
  }

  get partySelectOptions(): Array<{ id: number | null; label: string }> {
    return [{ id: null, label: '—' }, ...(this.parties || [])];
  }

  statusCount(status: string): number {
    const needle = (status || '').toUpperCase();
    return (this.rows || []).filter((row) => String(row['status'] ?? '').toUpperCase() === needle).length;
  }
}
