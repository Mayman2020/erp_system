import { ChangeDetectorRef } from '@angular/core';
import { FormArray, FormBuilder, FormGroup } from '@angular/forms';
import { Observable, Subject } from 'rxjs';
import { finalize, takeUntil } from 'rxjs/operators';
import { ProductDto, WarehouseDto } from '../../core/models/erp.models';
import { ConfirmDialogService } from '../../core/services/confirm-dialog.service';
import { DataTableAction, DataTableColumn } from '../components/data-table/data-table.component';
import {
  calcDocumentTotals,
  calcReturnTotals,
  createFullLineGroup,
  createSimpleLineGroup,
  DOCUMENT_ACTIONS,
  mapAmountRow,
  patchFullLines,
  patchSimpleLines
} from '../utils/erp-document.utils';

export type DocumentPageMode = 'full' | 'return';

export interface DocumentPageConfig {
  titleKey: string;
  numberKey: string;
  dateKey: string;
  partyKey: string;
  partyField: string;
  numberField: string;
  dateField: string;
  mode: DocumentPageMode;
  priceField: 'salePrice' | 'costPrice';
  showWarehouse?: boolean;
  showValidUntil?: boolean;
  showDueDate?: boolean;
  dueDateField?: string;
  showHeaderTax?: boolean;
  showInvoiceLink?: boolean;
}

export abstract class ErpDocumentPageBase<TDto, TForm> {
  readonly actions: DataTableAction[] = DOCUMENT_ACTIONS;
  readonly statusOptions = ['DRAFT', 'APPROVED', 'CANCELLED'];

  loading = false;
  saving = false;
  errorKey = '';
  successKey = '';
  rows: Array<Record<string, unknown>> = [];
  formVisible = false;
  formMode: 'create' | 'edit' | 'view' = 'create';
  selectedId: number | null = null;
  selectedAuditRecord: Record<string, unknown> | null = null;
  actorEmail = 'system@erp.local';

  parties: Array<{ id: number; label: string }> = [];
  products: ProductDto[] = [];
  warehouses: WarehouseDto[] = [];

  protected filters: Record<string, string> = {};
  protected readonly destroy$ = new Subject<void>();
  private feedbackTimer: ReturnType<typeof setTimeout> | null = null;

  abstract readonly config: DocumentPageConfig;
  abstract readonly columns: DataTableColumn[];
  abstract readonly form: FormGroup;

  constructor(
    protected fb: FormBuilder,
    protected confirmDialog: ConfirmDialogService,
    public cdr: ChangeDetectorRef
  ) {}

  get lines(): FormArray {
    return this.form.get('lines') as FormArray;
  }

  get readOnly(): boolean {
    return this.formMode === 'view';
  }

  get isReturnMode(): boolean {
    return this.config.mode === 'return';
  }

  initActor(refresh: () => void, actor$: Observable<{ email?: string; username?: string } | null>): void {
    actor$.pipe(takeUntil(this.destroy$)).subscribe((user) => {
      this.actorEmail = user?.email || user?.username || 'system@erp.local';
      this.cdr.markForCheck();
    });
    refresh();
  }

  destroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    if (this.feedbackTimer) {
      clearTimeout(this.feedbackTimer);
    }
  }

  onSearch(filters: Record<string, string>): void {
    this.filters = filters || {};
    this.reloadList();
  }

  openCreate(): void {
    this.formMode = 'create';
    this.selectedId = null;
    this.selectedAuditRecord = null;
    this.resetForm();
    this.form.enable();
    this.formVisible = true;
    this.errorKey = '';
    this.cdr.markForCheck();
  }

  closeForm(): void {
    this.formVisible = false;
    this.selectedAuditRecord = null;
    this.cdr.markForCheck();
  }

  onTableAction(event: { actionId: string; row: Record<string, unknown> }): void {
    const id = Number(event.row['id']);
    if (!id) {
      return;
    }
    if (event.actionId === 'view') {
      this.openDocument(id, 'view');
      return;
    }
    if (event.actionId === 'edit') {
      this.openDocument(id, 'edit');
      return;
    }
    if (event.actionId === 'approve') {
      this.confirmApprove(id);
      return;
    }
    if (event.actionId === 'cancel') {
      this.confirmCancel(id);
      return;
    }
    if (event.actionId === 'delete') {
      this.confirmDelete(id);
    }
  }

  save(): void {
    if (this.readOnly || this.form.invalid || this.lines.length === 0) {
      this.form.markAllAsTouched();
      this.showError('INVOICES.VALIDATION_REQUIRED');
      return;
    }
    this.saving = true;
    this.errorKey = '';
    const payload = this.buildPayload();
    const request$ = this.formMode === 'edit' && this.selectedId
      ? this.updateRequest(this.selectedId, payload)
      : this.createRequest(payload);
    request$.pipe(finalize(() => {
      this.saving = false;
      this.cdr.markForCheck();
    })).subscribe({
      next: () => {
        this.showSuccess('INVOICES.SAVE_SUCCESS');
        this.formVisible = false;
        this.reloadList();
      },
      error: (err) => this.showError(err?.error?.message || 'COMMON.UNEXPECTED_ERROR')
    });
  }

  approveCurrent(): void {
    if (this.selectedId) {
      this.confirmApprove(this.selectedId);
    }
  }

  documentTotals(): { subtotal: number; tax: number; total: number } {
    const raw = this.form.getRawValue();
    const lineValues = (raw.lines || []) as Array<Record<string, unknown>>;
    if (this.isReturnMode) {
      return calcReturnTotals(
        lineValues.map((l) => ({ quantity: Number(l['quantity']), unitPrice: Number(l['unitPrice']) })),
        Number(raw.taxAmount || 0)
      );
    }
    return calcDocumentTotals(
      lineValues.map((l) => ({
        quantity: Number(l['quantity']),
        unitPrice: Number(l['unitPrice']),
        discountPercent: Number(l['discountPercent'] || 0),
        taxPercent: Number(l['taxPercent'] || 0)
      })),
      Number(raw.discountAmount || 0)
    );
  }

  protected resetForm(): void {
    const today = new Date().toISOString().slice(0, 10);
    const patch: Record<string, unknown> = {
      [this.config.numberField]: '',
      [this.config.dateField]: today,
      [this.config.partyField]: null,
      notes: '',
      lines: []
    };
    if (this.config.showWarehouse) {
      patch['warehouseId'] = null;
    }
    if (this.config.showValidUntil) {
      patch['validUntil'] = today;
    }
    if (this.config.showDueDate) {
      patch[this.config.dueDateField || 'dueDate'] = today;
    }
    if (this.config.mode === 'full') {
      patch['discountAmount'] = 0;
    }
    if (this.config.showHeaderTax) {
      patch['taxAmount'] = 0;
    }
    if (this.config.showInvoiceLink) {
      patch['invoiceId'] = null;
    }
    this.form.reset(patch);
    this.lines.clear();
    this.lines.push(this.isReturnMode ? createSimpleLineGroup(this.fb) : createFullLineGroup(this.fb));
  }

  protected patchDocument(doc: Record<string, unknown>): void {
    const patch: Record<string, unknown> = { ...doc };
    this.form.reset(patch);
    this.lines.clear();
    const docLines = (doc['lines'] as Array<Record<string, unknown>>) || [];
    if (this.isReturnMode) {
      patchSimpleLines(this.fb, this.lines, docLines.map((l) => ({
        productId: Number(l['productId']),
        quantity: Number(l['quantity']),
        unitPrice: Number(l['unitPrice'])
      })));
    } else {
      patchFullLines(this.fb, this.lines, docLines.map((l) => ({
        productId: Number(l['productId']),
        description: String(l['description'] || ''),
        quantity: Number(l['quantity']),
        unitPrice: Number(l['unitPrice']),
        discountPercent: Number(l['discountPercent'] || 0),
        taxPercent: Number(l['taxPercent'] || 0)
      })));
    }
    if (this.lines.length === 0) {
      this.lines.push(this.isReturnMode ? createSimpleLineGroup(this.fb) : createFullLineGroup(this.fb));
    }
  }

  protected mapListRow(row: Record<string, unknown>): Record<string, unknown> {
    const mapped = mapAmountRow(row);
    const partyId = Number(row[this.config.partyField.replace('Id', '') + 'Id'] || row[this.config.partyField]);
    const party = this.parties.find((p) => p.id === partyId);
    if (party) {
      mapped[this.config.partyKey.replace('MENU.', '').toLowerCase() + 'Name'] = party.label;
    }
    return mapped;
  }

  protected openDocument(id: number, mode: 'edit' | 'view'): void {
    this.loading = true;
    this.fetchById(id).pipe(finalize(() => {
      this.loading = false;
      this.cdr.markForCheck();
    })).subscribe({
      next: (doc) => {
        this.formMode = mode;
        this.selectedId = id;
        this.selectedAuditRecord = doc as unknown as Record<string, unknown>;
        this.patchDocument(this.selectedAuditRecord);
        mode === 'view' ? this.form.disable() : this.form.enable();
        this.formVisible = true;
        this.cdr.markForCheck();
      },
      error: () => this.showError('COMMON.ERROR_LOADING')
    });
  }

  protected confirmApprove(id: number): void {
    this.confirmDialog.confirmByKey({ messageKey: 'INVOICES.APPROVE_CONFIRM' }).subscribe((ok) => {
      if (!ok) {
        return;
      }
      this.approveRequest(id, this.actorEmail).subscribe({
        next: () => {
          this.showSuccess('INVOICES.APPROVE_SUCCESS');
          this.formVisible = false;
          this.reloadList();
        },
        error: (err) => this.showError(err?.error?.message || 'COMMON.UNEXPECTED_ERROR')
      });
    });
  }

  protected confirmCancel(id: number): void {
    this.confirmDialog.confirmByKey({ messageKey: 'INVOICES.CANCEL_CONFIRM', danger: true }).subscribe((ok) => {
      if (!ok) {
        return;
      }
      this.cancelRequest(id, this.actorEmail).subscribe({
        next: () => {
          this.showSuccess('INVOICES.CANCEL_SUCCESS');
          this.formVisible = false;
          this.reloadList();
        },
        error: (err) => this.showError(err?.error?.message || 'COMMON.UNEXPECTED_ERROR')
      });
    });
  }

  protected confirmDelete(id: number): void {
    this.confirmDialog.confirmByKey({ messageKey: 'INVOICES.DELETE_CONFIRM', danger: true }).subscribe((ok) => {
      if (!ok) {
        return;
      }
      this.deleteRequest(id).subscribe({
        next: () => {
          this.showSuccess('INVOICES.DELETE_SUCCESS');
          this.reloadList();
        },
        error: (err) => this.showError(err?.error?.message || 'COMMON.UNEXPECTED_ERROR')
      });
    });
  }

  protected showError(key: string): void {
    this.errorKey = key;
    this.successKey = '';
    this.cdr.markForCheck();
  }

  protected showSuccess(key: string): void {
    this.successKey = key;
    this.errorKey = '';
    if (this.feedbackTimer) {
      clearTimeout(this.feedbackTimer);
    }
    this.feedbackTimer = setTimeout(() => {
      this.successKey = '';
      this.cdr.markForCheck();
    }, 4000);
    this.cdr.markForCheck();
  }

  protected abstract reloadList(): void;
  protected abstract fetchById(id: number): Observable<TDto>;
  protected abstract createRequest(payload: TForm): Observable<TDto>;
  protected abstract updateRequest(id: number, payload: TForm): Observable<TDto>;
  protected abstract deleteRequest(id: number): Observable<void>;
  protected abstract approveRequest(id: number, actor: string): Observable<TDto>;
  protected abstract cancelRequest(id: number, actor: string): Observable<TDto>;
  protected abstract buildPayload(): TForm;
}
