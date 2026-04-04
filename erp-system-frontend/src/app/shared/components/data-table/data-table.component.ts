import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import * as XLSX from 'xlsx';
import { TranslationService } from '../../../core/i18n/translation.service';
import { DateFormatService } from '../../../core/services/date-format.service';

export interface DataTableColumn {
  key: string;
  title: string;
  kind?: 'text' | 'status' | 'boolean' | 'type' | 'date' | 'localized' | 'booleanToggle';
  prefix?: string;
  clickable?: boolean;
  align?: 'start' | 'center' | 'end';
  className?: string;
}

export interface DataTableAction {
  id: string;
  labelKey: string;
  className: string;
  icon?: string;
  /** When true, the action button is shown but not clickable for that row. */
  disabledWhen?: (row: Record<string, unknown>) => boolean;
}

@Component({ standalone: false,
  selector: 'app-data-table',
  templateUrl: './data-table.component.html'
})
export class DataTableComponent implements OnChanges {
  @Input() columns: DataTableColumn[] = [];
  @Input() data: Array<Record<string, unknown>> = [];
  @Input() actions: DataTableAction[] = [];
  @Input() loading = false;
  @Input() pageSize = 5;
  @Input() page = 1;
  @Input() totalItems: number | null = null;
  @Input() paginationMode: 'client' | 'server' = 'client';
  @Input() compact = false;
  @Input() emptyTitleKey = 'COMMON.NO_DATA';
  @Input() emptyDescriptionKey = 'COMMON.NO_RESULTS_HINT';
  @Input() exportable = true;
  @Input() exportFileName = 'export';
  @Output() actionClick = new EventEmitter<{ actionId: string; row: Record<string, unknown> }>();
  @Output() cellClick = new EventEmitter<{ key: string; row: Record<string, unknown> }>();
  @Output() booleanToggle = new EventEmitter<{ key: string; row: Record<string, unknown>; checked: boolean }>();
  @Output() pageChange = new EventEmitter<number>();

  constructor(
    private dateFormatService: DateFormatService,
    private translationService: TranslationService
  ) {}

  currentPage = 1;
  readonly siblingCount = 1;

  get totalPages(): number {
    const size = Math.max(1, this.pageSize || 5);
    const itemCount = this.paginationMode === 'server' ? Math.max(0, this.totalItems ?? 0) : (this.data || []).length;
    return Math.max(1, Math.ceil(itemCount / size));
  }

  get visibleRows(): Array<Record<string, unknown>> {
    if (this.paginationMode === 'server') {
      return this.data || [];
    }
    const size = Math.max(1, this.pageSize || 5);
    const start = (this.currentPage - 1) * size;
    return (this.data || []).slice(start, start + size);
  }

  visiblePages(): number[] {
    const total = this.totalPages;
    if (total <= 1) {
      return [];
    }
    const current = Math.min(Math.max(1, this.currentPage), total);
    const start = Math.max(1, current - this.siblingCount);
    const end = Math.min(total, current + this.siblingCount);
    const pages: number[] = [];
    for (let page = start; page <= end; page += 1) {
      pages.push(page);
    }
    return pages;
  }

  hasLeadingJump(): boolean {
    const pages = this.visiblePages();
    return pages.length > 0 && pages[0] > 1;
  }

  hasTrailingJump(): boolean {
    const pages = this.visiblePages();
    return pages.length > 0 && pages[pages.length - 1] < this.totalPages;
  }

  setPage(page: number): void {
    if (page === this.currentPage || page < 1 || page > this.totalPages) {
      return;
    }
    this.currentPage = page;
    if (this.paginationMode === 'server') {
      this.pageChange.emit(page);
    }
  }

  onCellClick(column: DataTableColumn, row: Record<string, unknown>): void {
    if (!column.clickable) {
      return;
    }
    this.cellClick.emit({ key: column.key, row });
  }

  isActionDisabled(action: DataTableAction, row: Record<string, unknown>): boolean {
    return action.disabledWhen ? action.disabledWhen(row) : false;
  }

  onActionClick(action: DataTableAction, row: Record<string, unknown>): void {
    if (this.isActionDisabled(action, row)) {
      return;
    }
    this.actionClick.emit({ actionId: action.id, row });
  }

  onBooleanToggle(column: DataTableColumn, row: Record<string, unknown>, checked: boolean): void {
    this.booleanToggle.emit({ key: column.key, row, checked });
  }

  /** Stable row identity so *ngFor does not recreate the whole tbody when the parent passes a new array reference each CD. */
  trackRowByKey(index: number, row: Record<string, unknown>): unknown {
    if (!row) {
      return index;
    }
    const id = row['id'];
    if (id !== undefined && id !== null && `${id}` !== '') {
      return id;
    }
    const code = row['code'];
    if (code !== undefined && code !== null && `${code}` !== '') {
      return code;
    }
    const username = row['username'];
    if (username !== undefined && username !== null && `${username}` !== '') {
      return username;
    }
    return index;
  }

  isPriorityTextColumn(column: DataTableColumn): boolean {
    const key = (column.key || '').toLowerCase();
    return key === 'name' || key.endsWith('name') || key === 'description' || key.endsWith('description');
  }

  isDateColumn(column: DataTableColumn): boolean {
    return column.kind === 'date' || this.dateFormatService.isDateKey(column.key);
  }

  exportToExcel(): void {
    if (!this.data?.length) {
      return;
    }
    const headers = this.columns.map(col => this.translationService.instant(col.title));
    const rows = this.data.map(row =>
      this.columns.map(col => {
        const val = row[col.key];
        if (val === null || val === undefined) return '';
        if (this.isDateColumn(col)) return this.dateFormatService.format(val);
        return String(val);
      })
    );
    const wsData = [headers, ...rows];
    const ws = XLSX.utils.aoa_to_sheet(wsData);
    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, 'Sheet1');
    XLSX.writeFile(wb, this.exportFileName + '.xlsx');
  }

  formatCellValue(column: DataTableColumn, row: Record<string, unknown>, value: unknown): string {
    if (column.kind === 'localized') {
      const localizedValue = this.resolveLocalizedCellValue(column, row);
      if (localizedValue) {
        return localizedValue;
      }
    }
    if (value === null || value === undefined || value === '') {
      return '';
    }
    if (this.isDateColumn(column)) {
      return this.dateFormatService.format(value);
    }
    return `${value}`;
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['page']) {
      this.currentPage = Math.max(1, Number(this.page || 1));
    }
    if (changes['pageSize']) {
      this.syncCurrentPage();
    }
    if (changes['data']) {
      const prev = changes['data'].previousValue as unknown[] | undefined;
      const curr = changes['data'].currentValue as unknown[] | undefined;
      const prevLen = prev?.length ?? 0;
      const currLen = curr?.length ?? 0;
      if (prev === undefined || prevLen !== currLen) {
        this.syncCurrentPage();
      }
    }
    if (changes['totalItems']) {
      this.syncCurrentPage();
    }
  }

  private syncCurrentPage(): void {
    const totalPages = this.totalPages;
    if (this.currentPage < 1) {
      this.currentPage = 1;
      return;
    }
    if (this.currentPage > totalPages) {
      this.currentPage = totalPages;
    }
  }

  resolveActionIcon(action: DataTableAction): string {
    if (action.icon && action.icon.trim()) {
      return action.icon;
    }
    switch (action.id) {
      case 'edit':
      case 'update':
        return 'edit';
      case 'delete':
      case 'remove':
        return 'delete';
      case 'restore':
        return 'refresh';
      case 'view':
      case 'details':
        return 'visibility';
      case 'approve':
        return 'task_alt';
      case 'post':
        return 'publish';
      case 'cancel':
        return 'block';
      case 'toggle':
        return 'toggle_on';
      case 'reverse':
        return 'undo';
      default:
        return 'more_horiz';
    }
  }

  resolveActionTone(action: DataTableAction): string {
    if ((action.className || '').indexOf('danger') > -1) {
      return 'danger';
    }
    if ((action.className || '').indexOf('warning') > -1) {
      return 'warning';
    }
    if ((action.className || '').indexOf('success') > -1) {
      return 'success';
    }
    if ((action.className || '').indexOf('secondary') > -1) {
      return 'neutral';
    }
    return 'info';
  }

  private resolveLocalizedCellValue(column: DataTableColumn, row: Record<string, unknown>): string {
    if (!row || (!column.key && column.kind !== 'localized')) {
      return '';
    }

    const baseKey = (column.key || '').replace(/(Ar|En)$/i, '');
    const preferredSuffix = this.translationService.currentLanguage === 'ar' ? 'Ar' : 'En';
    const alternateSuffix = preferredSuffix === 'Ar' ? 'En' : 'Ar';
    const preferredKeys = [`${baseKey}${preferredSuffix}`, `${column.key}${preferredSuffix}`];
    const alternateKeys = [`${baseKey}${alternateSuffix}`, `${column.key}${alternateSuffix}`];

    const preferredValue = this.resolveFirstStringValue(row, preferredKeys);
    if (preferredValue) {
      return preferredValue;
    }

    return this.resolveFirstStringValue(row, alternateKeys);
  }

  private resolveFirstStringValue(row: Record<string, unknown>, keys: string[]): string {
    for (const key of keys) {
      const value = row[key];
      if (typeof value === 'string' && value.trim()) {
        return value;
      }
    }
    return '';
  }
}
