import { ChangeDetectorRef } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { Observable, Subject } from 'rxjs';
import { finalize, takeUntil } from 'rxjs/operators';
import { AuthService } from '../../core/auth/auth.service';
import { ConfirmDialogService } from '../../core/services/confirm-dialog.service';
import { DataTableAction, DataTableColumn } from '../components/data-table/data-table.component';

export const MASTER_CRUD_ACTIONS: DataTableAction[] = [
  { id: 'view', labelKey: 'COMMON.VIEW', className: 'erp-action-secondary' },
  { id: 'edit', labelKey: 'COMMON.EDIT', className: 'erp-action-info' },
  { id: 'delete', labelKey: 'COMMON.DELETE', className: 'erp-action-danger' }
];

export interface MasterPageConfig {
  titleKey: string;
  createKey: string;
  editKey: string;
  viewKey: string;
  menuItemId?: string;
  createPermission?: 'canCreate' | 'canEdit' | 'canDelete' | 'canView';
  deleteConfirmKey?: string;
  saveSuccessKey?: string;
  deleteSuccessKey?: string;
  showSearch?: boolean;
  showStatus?: boolean;
  showDateRange?: boolean;
  statusOptions?: string[];
}

export abstract class ErpMasterPageBase<TDto extends { id: number }, TForm> {
  readonly actions: DataTableAction[] = MASTER_CRUD_ACTIONS;

  loading = false;
  saving = false;
  errorKey = '';
  successKey = '';
  rows: Array<Record<string, unknown>> = [];
  formVisible = false;
  formMode: 'create' | 'edit' | 'view' = 'create';
  selectedId: number | null = null;
  actorEmail = 'system@erp.local';

  protected filters: Record<string, string> = {};
  protected readonly destroy$ = new Subject<void>();
  private feedbackTimer: ReturnType<typeof setTimeout> | null = null;

  abstract readonly config: MasterPageConfig;
  abstract readonly columns: DataTableColumn[];
  abstract readonly form: FormGroup;

  constructor(
    protected authService: AuthService,
    protected confirmDialog: ConfirmDialogService,
    public cdr: ChangeDetectorRef
  ) {}

  get titleKey(): string {
    return this.config.titleKey;
  }

  get showSearch(): boolean {
    return this.config.showSearch !== false;
  }

  get showStatus(): boolean {
    return !!this.config.showStatus;
  }

  get showDateRange(): boolean {
    return !!this.config.showDateRange;
  }

  get statusOptions(): string[] {
    return this.config.statusOptions || [];
  }

  get readOnly(): boolean {
    return this.formMode === 'view';
  }

  initMasterPage(): void {
    this.authService.currentUser$.pipe(takeUntil(this.destroy$)).subscribe((user) => {
      this.actorEmail = user?.email || user?.username || 'system@erp.local';
      this.cdr.markForCheck();
    });
    this.authService.refreshCurrentUser();
    this.load();
  }

  destroyMasterPage(): void {
    this.destroy$.next();
    this.destroy$.complete();
    if (this.feedbackTimer) {
      clearTimeout(this.feedbackTimer);
    }
  }

  onSearch(filters: Record<string, string>): void {
    this.filters = filters || {};
    this.load();
  }

  openCreate(): void {
    this.formMode = 'create';
    this.selectedId = null;
    this.form.reset(this.defaultFormValues());
    this.form.enable();
    this.formVisible = true;
    this.errorKey = '';
    this.cdr.markForCheck();
  }

  closeForm(): void {
    this.formVisible = false;
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
    if (event.actionId === 'delete') {
      this.confirmDelete(id);
    }
  }

  save(): void {
    if (this.readOnly || this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving = true;
    const payload = this.toPayload();
    const request$ = this.formMode === 'edit' && this.selectedId
      ? this.updateItem(this.selectedId, payload)
      : this.createItem(payload);
    request$
      .pipe(finalize(() => {
        this.saving = false;
        this.cdr.markForCheck();
      }))
      .subscribe({
        next: () => {
          this.showSuccess(this.config.saveSuccessKey || 'COMMON.SAVE_SUCCESS');
          this.formVisible = false;
          this.load();
        },
        error: (err) => this.showError(err?.error?.message || 'COMMON.UNEXPECTED_ERROR')
      });
  }

  protected load(): void {
    this.loading = true;
    this.errorKey = '';
    const params = this.buildListParams();
    this.fetchList(params)
      .pipe(finalize(() => {
        this.loading = false;
        this.cdr.markForCheck();
      }))
      .subscribe({
        next: (rows) => {
          this.rows = (rows || []).map((row) => this.mapRow(row));
        },
        error: () => {
          this.errorKey = 'COMMON.ERROR_LOADING';
          this.rows = [];
        }
      });
  }

  protected openDocument(id: number, mode: 'edit' | 'view'): void {
    this.fetchOne(id).subscribe({
      next: (item) => {
        this.formMode = mode;
        this.selectedId = item.id;
        this.patchForm(item);
        if (mode === 'view') {
          this.form.disable();
        } else {
          this.form.enable();
        }
        this.formVisible = true;
        this.cdr.markForCheck();
      },
      error: () => this.showError('COMMON.ERROR_LOADING')
    });
  }

  protected confirmDelete(id: number): void {
    this.confirmDialog.confirmByKey({
      messageKey: this.config.deleteConfirmKey || 'COMMON.DELETE_CONFIRM',
      danger: true
    }).subscribe((ok) => {
      if (!ok) {
        return;
      }
      this.removeItem(id).subscribe({
        next: () => {
          this.showSuccess(this.config.deleteSuccessKey || 'COMMON.DELETE_SUCCESS');
          this.load();
        },
        error: (err) => this.showError(err?.error?.message || 'COMMON.UNEXPECTED_ERROR')
      });
    });
  }

  protected buildListParams(): Record<string, string> {
    const params: Record<string, string> = { search: this.filters.query || '' };
    if (this.showStatus && this.filters.status) {
      params.status = this.filters.status;
    }
    if (this.showDateRange) {
      if (this.filters.fromDate) {
        params.fromDate = this.filters.fromDate;
      }
      if (this.filters.toDate) {
        params.toDate = this.filters.toDate;
      }
    }
    return params;
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

  protected abstract fetchList(filters: Record<string, string>): Observable<TDto[]>;
  protected abstract fetchOne(id: number): Observable<TDto>;
  protected abstract createItem(payload: TForm): Observable<TDto>;
  protected abstract updateItem(id: number, payload: TForm): Observable<TDto>;
  protected abstract removeItem(id: number): Observable<void>;
  protected abstract defaultFormValues(): Record<string, unknown>;
  protected abstract patchForm(dto: TDto): void;
  protected abstract toPayload(): TForm;
  protected abstract mapRow(dto: TDto): Record<string, unknown>;
}
