import { ChangeDetectionStrategy, ChangeDetectorRef, Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { exportAoAToStyledExcel } from '../../../core/utils/styled-excel-export';
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
  sortable?: boolean;
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
  templateUrl: './data-table.component.html',
  styleUrls: ['./data-table.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DataTableComponent implements OnChanges {
  @Input() columns: DataTableColumn[] = [];
  @Input() data: Array<Record<string, unknown>> = [];
  @Input() actions: DataTableAction[] = [];
  /** Max data columns shown in the grid; remaining + extra row keys appear in the details dialog. */
  @Input() maxSummaryColumns = 5;
  /** When true and there are more than `maxSummaryColumns` columns (or extra row fields), show a details icon. */
  @Input() rowDetailsEnabled = true;
  @Input() loading = false;
  @Input() skeletonRows = 5;
  @Input() pageSize = 5;
  @Input() page = 1;
  @Input() totalItems: number | null = null;
  @Input() paginationMode: 'client' | 'server' = 'client';
  @Input() compact = false;
  @Input() emptyTitleKey = 'COMMON.NO_DATA';
  @Input() emptyDescriptionKey = 'COMMON.NO_RESULTS_HINT';
  @Input() exportable = true;
  @Input() filterable = false;
  @Input() exportFileName = 'export';

  @Output() actionClick = new EventEmitter<{ actionId: string; row: Record<string, unknown> }>();
  @Output() cellClick = new EventEmitter<{ key: string; row: Record<string, unknown> }>();
  @Output() booleanToggle = new EventEmitter<{ key: string; row: Record<string, unknown>; checked: boolean }>();
  @Output() pageChange = new EventEmitter<number>();
  @Output() sortChange = new EventEmitter<{ key: string; direction: 'asc' | 'desc' }>();
  @Output() filterChange = new EventEmitter<string>();

  globalFilter = '';
  sortKey: string | null = null;
  sortDirection: 'asc' | 'desc' = 'asc';
  currentPage = 1;
  readonly siblingCount = 1;

  /** Row opened in the read-only details dialog */
  detailRow: Record<string, unknown> | null = null;
  extraDetailRows: Array<{ label: string; value: string }> = [];

  constructor(
    private dateFormatService: DateFormatService,
    private translationService: TranslationService,
    private cdr: ChangeDetectorRef
  ) {}

  get effectiveSummaryLimit(): number {
    return Math.max(1, Number(this.maxSummaryColumns) || 5);
  }

  get summaryColumns(): DataTableColumn[] {
    const cols = this.columns || [];
    return cols.slice(0, this.effectiveSummaryLimit);
  }

  get showRowDetailControl(): boolean {
    if (!this.rowDetailsEnabled) {
      return false;
    }
    const cols = this.columns || [];
    return cols.length > this.effectiveSummaryLimit;
  }

  get hasActionsColumn(): boolean {
    return (this.actions?.length || 0) > 0 || this.showRowDetailControl;
  }

  openRowDetails(row: Record<string, unknown>): void {
    this.detailRow = row;
    const colKeys = new Set((this.columns || []).map((c) => c.key));
    this.extraDetailRows = Object.keys(row || {})
      .filter((k) => !colKeys.has(k) && !k.startsWith('_'))
      .sort()
      .map((k) => ({
        label: this.humanizeExtraKey(k),
        value: this.stringifyDetailValue((row as Record<string, unknown>)[k])
      }));
    this.cdr.markForCheck();
  }

  closeRowDetails(): void {
    this.detailRow = null;
    this.extraDetailRows = [];
    this.cdr.markForCheck();
  }

  private humanizeExtraKey(key: string): string {
    return (key || '')
      .replace(/([A-Z])/g, ' $1')
      .replace(/^./, (s) => s.toUpperCase())
      .trim();
  }

  private stringifyDetailValue(value: unknown): string {
    if (value === null || value === undefined) {
      return '';
    }
    if (typeof value === 'object') {
      try {
        return JSON.stringify(value);
      } catch {
        return String(value);
      }
    }
    return String(value);
  }

  get processedData(): Array<Record<string, unknown>> {
    let source = this.data || [];
    
    // 1. Filter
    if (this.paginationMode === 'client' && this.globalFilter) {
      const q = this.globalFilter.toLowerCase();
      source = source.filter(row => {
        return this.columns.some(col => {
          const val = this.formatCellValue(col, row, row[col.key]);
          return val && val.toLowerCase().includes(q);
        }) || this.extraRowKeysMatchFilter(row, q);
      });
    }

    // 2. Sort
    if (this.paginationMode === 'client' && this.sortKey) {
      source = [...source].sort((a, b) => {
        const valA = a[this.sortKey!];
        const valB = b[this.sortKey!];
        
        let comparison = 0;
        if (typeof valA === 'number' && typeof valB === 'number') {
          comparison = valA - valB;
        } else {
          comparison = String(valA || '').localeCompare(String(valB || ''));
        }
        return this.sortDirection === 'asc' ? comparison : -comparison;
      });
    }

    return source;
  }

  get totalPages(): number {
    const size = Math.max(1, this.pageSize || 5);
    const itemCount = this.paginationMode === 'server' ? Math.max(0, this.totalItems ?? 0) : this.processedData.length;
    return Math.max(1, Math.ceil(itemCount / size));
  }

  get visibleRows(): Array<Record<string, unknown>> {
    if (this.paginationMode === 'server') {
      return this.data || [];
    }
    const size = this.currentPageSize;
    const start = (this.currentPage - 1) * size;
    return this.processedData.slice(start, start + size);
  }

  get currentPageSize(): number {
    return Math.max(1, this.pageSize || 5);
  }

  get totalItemsCount(): number {
    return this.paginationMode === 'server' ? (this.totalItems ?? 0) : this.processedData.length;
  }

  get rangeStart(): number {
    if (this.totalItemsCount === 0) return 0;
    return (this.currentPage - 1) * this.currentPageSize + 1;
  }

  get rangeEnd(): number {
    return Math.min(this.currentPage * this.currentPageSize, this.totalItemsCount);
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
    this.cdr.markForCheck();
  }

  onGlobalFilter(event: Event): void {
    const target = event.target as HTMLInputElement;
    this.globalFilter = target.value;
    this.currentPage = 1;
    this.filterChange.emit(this.globalFilter);
    this.cdr.markForCheck();
  }

  getSkeletonArray(): number[] {
    return Array(this.skeletonRows).fill(0);
  }

  onSort(column: DataTableColumn): void {
    if (!column.sortable) {
      return;
    }
    if (this.sortKey === column.key) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortKey = column.key;
      this.sortDirection = 'asc';
    }
    this.currentPage = 1;
    this.sortChange.emit({ key: this.sortKey, direction: this.sortDirection });
    this.cdr.markForCheck();
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
      this.columns.map(col => this.formatCellValue(col, row, row[col.key]))
    );
    const wsData = [headers, ...rows];
    exportAoAToStyledExcel(wsData, {
      sheetName: 'Export',
      fileName: this.exportFileName,
      headerRows: [0],
      rightAlignColumns: []
    });
  }

  formatCellValue(column: DataTableColumn, row: Record<string, unknown>, value: unknown): string {
    if (column.kind === 'localized') {
      const localizedValue = this.resolveLocalizedCellValue(column, row);
      if (localizedValue) {
        return localizedValue;
      }
    }
    if (row && column.kind !== 'localized') {
      const sibling = this.resolveSiblingLocalizedField(row, column.key);
      if (sibling) {
        return sibling;
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
      if (!changes['data'].firstChange && prev !== curr) {
        this.currentPage = 1;
      }
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

  /**
   * When a column binds to `name` / `accountName` / `description` etc. and the row has *Ar / *En
   * siblings, show the value for the active UI language.
   */
  private extraRowKeysMatchFilter(row: Record<string, unknown>, q: string): boolean {
    const colKeys = new Set((this.columns || []).map((c) => c.key));
    for (const k of Object.keys(row)) {
      if (colKeys.has(k) || k.startsWith('_')) {
        continue;
      }
      const v = row[k];
      if (v !== null && v !== undefined && String(v).toLowerCase().includes(q)) {
        return true;
      }
    }
    return false;
  }

  private resolveSiblingLocalizedField(row: Record<string, unknown>, key: string): string {
    if (!key || key.endsWith('Ar') || key.endsWith('En')) {
      return '';
    }
    const arKey = `${key}Ar`;
    const enKey = `${key}En`;
    const arVal = row[arKey];
    const enVal = row[enKey];
    const ar = typeof arVal === 'string' ? arVal.trim() : '';
    const en = typeof enVal === 'string' ? enVal.trim() : '';
    if (!ar && !en) {
      return '';
    }
    if (this.translationService.currentLanguage === 'ar') {
      return ar || en;
    }
    return en || ar;
  }
}
