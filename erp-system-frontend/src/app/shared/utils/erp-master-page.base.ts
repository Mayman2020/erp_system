import { ChangeDetectorRef, Optional } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { Observable, Subject } from 'rxjs';
import { finalize, takeUntil } from 'rxjs/operators';
import { AuthService } from '../../core/auth/auth.service';
import { ConfirmDialogService } from '../../core/services/confirm-dialog.service';
import { PermissionService } from '../../core/services/permission.service';
import { PagedResult } from '../../core/services/erp-api.service';
import {
  clampTablePageIndex,
  DEFAULT_TABLE_PAGE_SIZE,
  paginatedSlice,
  withPageParams
} from '../../core/utils/pagination.util';
import { DataTableAction, DataTableColumn } from '../components/data-table/data-table.component';
import { ExportColumn } from '../components/table-export-toolbar/table-export-toolbar.component';
import { ListLoadController } from './list-load.util';

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

export abstract class ErpMasterPageBase<TDto extends {
  id: number;
  createdAt?: string;
  updatedAt?: string;
  createdBy?: string;
  updatedBy?: string;
}, TForm> {
  actions: DataTableAction[] = [...MASTER_CRUD_ACTIONS];
  readonly listLoad = new ListLoadController();

  loading = false;
  saving = false;
  errorKey = '';
  successKey = '';
  rows: Array<Record<string, unknown>> = [];
  pageIndex = 0;
  pageSize = DEFAULT_TABLE_PAGE_SIZE;
  totalElements = 0;
  formVisible = false;
  formMode: 'create' | 'edit' | 'view' = 'create';
  selectedId: number | null = null;
  selectedAuditRecord: TDto | null = null;
  actorEmail = 'system@erp.local';
  canCreate = true;
  canEdit = true;
  canDelete = true;
  canExport = true;

  protected filters: Record<string, string> = {};
  protected readonly destroy$ = new Subject<void>();
  private feedbackTimer: ReturnType<typeof setTimeout> | null = null;

  abstract readonly config: MasterPageConfig;
  abstract readonly columns: DataTableColumn[];
  abstract readonly form: FormGroup;

  constructor(
    protected authService: AuthService,
    protected confirmDialog: ConfirmDialogService,
    public cdr: ChangeDetectorRef,
    @Optional() protected permissionService?: PermissionService
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

  /** Opt-in: list uses `/paged` APIs instead of client slicing. */
  protected readonly serverPaging: boolean = false;

  get pagedRows(): Array<Record<string, unknown>> {
    if (this.serverPaging) {
      return this.rows;
    }
    return paginatedSlice(this.rows, this.pageIndex, this.pageSize);
  }

  get exportFileName(): string {
    return (this.config.titleKey || 'export').replace(/[^a-zA-Z0-9_-]+/g, '-').toLowerCase();
  }

  get exportColumns(): ExportColumn[] {
    return this.columns.map((column) => ({
      header: column.title,
      value: column.key
    }));
  }

  initMasterPage(): void {
    this.authService.currentUser$.pipe(takeUntil(this.destroy$)).subscribe((user) => {
      this.actorEmail = user?.email || user?.username || 'system@erp.local';
      this.cdr.markForCheck();
    });
    this.authService.refreshCurrentUser();
    this.loadPermissions();
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
    this.pageIndex = 0;
    this.load();
  }

  onPageChange(pageIndex: number): void {
    if (this.pageIndex === pageIndex) {
      return;
    }
    this.pageIndex = pageIndex;
    if (this.serverPaging) {
      this.load(false);
      return;
    }
    this.cdr.markForCheck();
  }

  openCreate(): void {
    if (!this.canCreate) {
      this.showError('COMMON.FORBIDDEN');
      return;
    }
    this.formMode = 'create';
    this.selectedId = null;
    this.selectedAuditRecord = null;
    this.form.reset(this.defaultFormValues());
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
      if (!this.canEdit) {
        this.showError('COMMON.FORBIDDEN');
        return;
      }
      this.openDocument(id, 'edit');
      return;
    }
    if (event.actionId === 'delete') {
      if (!this.canDelete) {
        this.showError('COMMON.FORBIDDEN');
        return;
      }
      this.confirmDelete(id);
    }
  }

  save(): void {
    if (this.readOnly || this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    if (this.formMode === 'create' && !this.canCreate) {
      this.showError('COMMON.FORBIDDEN');
      return;
    }
    if (this.formMode === 'edit' && !this.canEdit) {
      this.showError('COMMON.FORBIDDEN');
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

  protected loadPermissions(): void {
    const menuItemId = this.config.menuItemId;
    if (!menuItemId || !this.permissionService) {
      return;
    }
    this.permissionService.can(menuItemId, 'canCreate').pipe(takeUntil(this.destroy$)).subscribe((ok) => {
      this.canCreate = ok;
      this.refreshActions();
    });
    this.permissionService.can(menuItemId, 'canEdit').pipe(takeUntil(this.destroy$)).subscribe((ok) => {
      this.canEdit = ok;
      this.refreshActions();
    });
    this.permissionService.can(menuItemId, 'canDelete').pipe(takeUntil(this.destroy$)).subscribe((ok) => {
      this.canDelete = ok;
      this.refreshActions();
    });
    this.permissionService.can(menuItemId, 'canView').pipe(takeUntil(this.destroy$)).subscribe((ok) => {
      this.canExport = ok;
      this.cdr.markForCheck();
    });
  }

  protected refreshActions(): void {
    this.actions = MASTER_CRUD_ACTIONS.filter((action) => {
      if (action.id === 'edit') {
        return this.canEdit;
      }
      if (action.id === 'delete') {
        return this.canDelete;
      }
      return true;
    });
    this.cdr.markForCheck();
  }

  protected load(resetPage = true): void {
    if (resetPage) {
      this.pageIndex = 0;
    }
    this.listLoad.begin();
    this.loading = this.listLoad.showInitialSpinner;
    this.errorKey = '';
    if (this.serverPaging) {
      this.loadServerPage();
      return;
    }
    const params = this.buildListParams();
    this.fetchList(params)
      .pipe(finalize(() => {
        this.listLoad.end();
        this.loading = this.listLoad.showInitialSpinner;
        this.cdr.markForCheck();
      }))
      .subscribe({
        next: (rows) => {
          this.rows = (rows || []).map((row) => this.mapRow(row));
          this.totalElements = this.rows.length;
          this.pageIndex = clampTablePageIndex(this.pageIndex, this.totalElements, this.pageSize);
        },
        error: () => {
          this.errorKey = 'COMMON.ERROR_LOADING';
          this.rows = [];
          this.totalElements = 0;
          this.pageIndex = 0;
        }
      });
  }

  private loadServerPage(): void {
    const params = withPageParams(this.pageIndex, this.pageSize, this.buildListParams());
    this.fetchPagedList(params)
      .pipe(finalize(() => {
        this.listLoad.end();
        this.loading = this.listLoad.showInitialSpinner;
        this.cdr.markForCheck();
      }))
      .subscribe({
        next: (page) => {
          const content = page?.content || [];
          this.rows = content.map((row) => this.mapRow(row));
          this.totalElements = page?.totalElements ?? content.length;
          this.pageIndex = clampTablePageIndex(
            page?.page ?? this.pageIndex,
            this.totalElements,
            this.pageSize
          );
        },
        error: () => {
          this.errorKey = 'COMMON.ERROR_LOADING';
          this.rows = [];
          this.totalElements = 0;
          this.pageIndex = 0;
        }
      });
  }

  protected openDocument(id: number, mode: 'edit' | 'view'): void {
    this.fetchOne(id).subscribe({
      next: (item) => {
        this.formMode = mode;
        this.selectedId = item.id;
        this.selectedAuditRecord = item;
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
    const query = this.filters.query || '';
    const params: Record<string, string> = this.serverPaging
      ? (query ? { q: query } : {})
      : { search: query };
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
  protected fetchPagedList(_filters: Record<string, string | number | boolean>): Observable<PagedResult<TDto>> {
    throw new Error(`${this.constructor.name} must implement fetchPagedList when serverPaging is enabled`);
  }
  protected abstract fetchOne(id: number): Observable<TDto>;
  protected abstract createItem(payload: TForm): Observable<TDto>;
  protected abstract updateItem(id: number, payload: TForm): Observable<TDto>;
  protected abstract removeItem(id: number): Observable<void>;
  protected abstract defaultFormValues(): Record<string, unknown>;
  protected abstract patchForm(dto: TDto): void;
  protected abstract toPayload(): TForm;
  protected abstract mapRow(dto: TDto): Record<string, unknown>;
}
