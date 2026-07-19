import { Component, Input } from '@angular/core';
import { TranslationService } from '../../../core/i18n/translation.service';

export interface ExportColumn<T = unknown> {
  header: string;
  value: keyof T | ((row: T, index: number) => unknown);
}

@Component({
  standalone: false,
  selector: 'app-table-export-toolbar',
  template: `
    <div class="erp-export-toolbar" *ngIf="showExport" [class.erp-export-toolbar--inline]="inline">
      <button
        type="button"
        class="erp-excel-export-btn"
        (click)="exportExcel()"
        [disabled]="disabled || !hasRows"
        [ngbTooltip]="excelLabel"
        placement="top"
        container="body"
        [attr.aria-label]="excelLabel">
        <mat-icon aria-hidden="true" class="erp-excel-export-btn__icon">table_chart</mat-icon>
        <span>{{ 'COMMON.EXPORT_EXCEL' | translate }}</span>
      </button>
    </div>
  `,
  styles: [`
    :host {
      display: inline-flex;
      align-items: center;
      flex-shrink: 0;
    }
    :host(.erp-export-host--block) {
      display: block;
      width: 100%;
    }
    .erp-export-toolbar {
      display: inline-flex;
      justify-content: flex-end;
      align-items: center;
      gap: 8px;
      min-height: 64px;
      padding: 12px 16px;
      flex-wrap: wrap;
      border-block: 1px solid var(--line-2, var(--line));
      background: var(--paper-2, transparent);
    }
    .erp-export-toolbar--inline {
      padding: 0;
      min-height: 0;
      border-block: 0;
      background: transparent;
      justify-content: flex-start;
    }
  `]
})
export class TableExportToolbarComponent<T = unknown> {
  @Input() inline = false;
  @Input() title = 'Export';
  @Input() fileName = 'export';
  @Input() showExport = true;
  @Input() columns: ExportColumn<T>[] = [];
  @Input() rows: T[] = [];
  @Input() loadRows?: () => Promise<T[]>;
  @Input() disabled = false;

  constructor(private readonly translationService: TranslationService) {}

  get hasRows(): boolean {
    return !!this.loadRows || this.rows.length > 0;
  }

  get excelLabel(): string {
    return this.translationService.instant('EXPORT.EXCEL');
  }

  async exportExcel(): Promise<void> {
    const table = this.tableRows(await this.resolveRows());
    import('xlsx-js-style').then((XLSX) => {
      const ws = XLSX.utils.aoa_to_sheet([this.headers(), ...table]);
      const wb = XLSX.utils.book_new();
      XLSX.utils.book_append_sheet(wb, ws, this.sheetName());
      XLSX.writeFile(wb, `${this.safeFileName()}-${this.fileDate()}.xlsx`);
    });
  }

  private headers(): string[] {
    return this.columns.map((column) => column.header);
  }

  private tableRows(rows: T[]): unknown[][] {
    return rows.map((row, rowIndex) =>
      this.columns.map((column) => {
        const value = typeof column.value === 'function'
          ? column.value(row, rowIndex)
          : (row as Record<string, unknown>)[String(column.value)];
        return value ?? '-';
      })
    );
  }

  private safeFileName(): string {
    return (this.fileName || this.title || 'export').replace(/[\\/:*?"<>|]+/g, '-').trim();
  }

  private sheetName(): string {
    return (this.title || this.fileName || 'Export').replace(/[\\/?*[\]:]+/g, ' ').slice(0, 31);
  }

  private resolveRows(): Promise<T[]> {
    return this.loadRows ? this.loadRows() : Promise.resolve(this.rows);
  }

  private fileDate(separator = '-'): string {
    const date = new Date();
    const day = String(date.getDate()).padStart(2, '0');
    const month = String(date.getMonth() + 1).padStart(2, '0');
    return `${day}${separator}${month}${separator}${date.getFullYear()}`;
  }
}
