import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { DateFormatService } from '../../../core/services/date-format.service';

export interface DataTableColumn {
  key: string;
  title: string;
  kind?: 'text' | 'status' | 'boolean' | 'type' | 'date';
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
}

@Component({
  selector: 'app-data-table',
  templateUrl: './data-table.component.html'
})
export class DataTableComponent implements OnChanges {
  readonly maxVisibleActions = 3;
  @Input() columns: DataTableColumn[] = [];
  @Input() data: Array<Record<string, unknown>> = [];
  @Input() actions: DataTableAction[] = [];
  @Input() loading = false;
  @Input() pageSize = 5;
  @Input() compact = false;
  @Input() emptyTitleKey = 'COMMON.NO_DATA';
  @Input() emptyDescriptionKey = 'COMMON.NO_RESULTS_HINT';
  @Output() actionClick = new EventEmitter<{ actionId: string; row: Record<string, unknown> }>();
  @Output() cellClick = new EventEmitter<{ key: string; row: Record<string, unknown> }>();

  constructor(private dateFormatService: DateFormatService) {}

  currentPage = 1;
  readonly siblingCount = 1;

  get totalPages(): number {
    const size = Math.max(1, this.pageSize || 5);
    return Math.max(1, Math.ceil((this.data || []).length / size));
  }

  get visibleRows(): Array<Record<string, unknown>> {
    const size = Math.max(1, this.pageSize || 5);
    const start = (this.currentPage - 1) * size;
    return (this.data || []).slice(start, start + size);
  }

  get visibleActions(): DataTableAction[] {
    return (this.actions || []).slice(0, this.maxVisibleActions);
  }

  get overflowActions(): DataTableAction[] {
    return (this.actions || []).slice(this.maxVisibleActions);
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
  }

  onCellClick(column: DataTableColumn, row: Record<string, unknown>): void {
    if (!column.clickable) {
      return;
    }
    this.cellClick.emit({ key: column.key, row });
  }

  isPriorityTextColumn(column: DataTableColumn): boolean {
    const key = (column.key || '').toLowerCase();
    return key === 'name' || key.endsWith('name') || key === 'description' || key.endsWith('description');
  }

  isDateColumn(column: DataTableColumn): boolean {
    return column.kind === 'date' || this.dateFormatService.isDateKey(column.key);
  }

  formatCellValue(column: DataTableColumn, value: unknown): string {
    if (value === null || value === undefined || value === '') {
      return '';
    }
    if (this.isDateColumn(column)) {
      return this.dateFormatService.format(value);
    }
    return `${value}`;
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['data']) {
      this.currentPage = 1;
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
}
