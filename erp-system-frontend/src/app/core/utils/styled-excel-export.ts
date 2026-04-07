import * as XLSX from 'xlsx-js-style';
import type { CellStyle, ColInfo, Range, RowInfo, WorkSheet } from 'xlsx-js-style';

const BORDER_THIN = {
  top: { style: 'thin' as const, color: { rgb: 'FFB0B0B0' } },
  bottom: { style: 'thin' as const, color: { rgb: 'FFB0B0B0' } },
  left: { style: 'thin' as const, color: { rgb: 'FFB0B0B0' } },
  right: { style: 'thin' as const, color: { rgb: 'FFB0B0B0' } }
};

/** Excel / Office green header band */
const HEADER_FILL = 'FF217346';
const HEADER_FONT = 'FFFFFFFF';
const SECTION_FILL = 'FFE8F5E9';

export interface ExportStyledExcelOptions {
  sheetName: string;
  fileName: string;
  /** 0-based rows treated as column headers (green band, white text, centered) */
  headerRows?: number[];
  /** 0-based columns right-aligned (e.g. amounts), excluding header rows */
  rightAlignColumns?: number[];
  /** 0-based rows: bold + light green band (section titles, totals) */
  boldRows?: number[];
}

function sanitizeSheetName(name: string): string {
  const s = name.replace(/[\\/?*[\]:]/g, '-').trim() || 'Sheet1';
  return s.length > 31 ? s.slice(0, 31) : s;
}

function ensureExtension(fileName: string): string {
  return fileName.toLowerCase().endsWith('.xlsx') ? fileName : `${fileName}.xlsx`;
}

/** Fill empty slots so borders cover the full grid */
function ensureRectangularGrid(ws: WorkSheet, range: Range): void {
  for (let r = range.s.r; r <= range.e.r; r++) {
    for (let c = range.s.c; c <= range.e.c; c++) {
      const addr = XLSX.utils.encode_cell({ r, c });
      if (!ws[addr]) {
        ws[addr] = { t: 's', v: '' };
      }
    }
  }
}

/**
 * Exports a 2D matrix to a styled .xlsx: wide columns, header row, thin grid borders.
 * Use from every Excel download in the app for a consistent look.
 */
export function exportAoAToStyledExcel(matrix: unknown[][], options: ExportStyledExcelOptions): void {
  const headerRows = options.headerRows?.length ? options.headerRows : [0];
  const headerSet = new Set(headerRows);
  const boldSet = new Set(options.boldRows ?? []);
  const rightSet = new Set(options.rightAlignColumns ?? []);

  const ws = XLSX.utils.aoa_to_sheet(matrix);
  const ref = ws['!ref'];
  if (!ref) {
    return;
  }

  const range = XLSX.utils.decode_range(ref);
  ensureRectangularGrid(ws, range);

  const cols: ColInfo[] = [];
  for (let c = range.s.c; c <= range.e.c; c++) {
    let maxLen = 16;
    for (let r = range.s.r; r <= range.e.r; r++) {
      const cell = ws[XLSX.utils.encode_cell({ r, c })];
      if (cell?.v != null && `${cell.v}`.length > 0) {
        maxLen = Math.max(maxLen, String(cell.v).replace(/\r?\n/g, ' ').length);
      }
    }
    cols.push({ wch: Math.min(Math.max(maxLen + 3, 18), 62) });
  }
  ws['!cols'] = cols;

  const rowsMeta: RowInfo[] = [];
  for (let r = range.s.r; r <= range.e.r; r++) {
    rowsMeta[r] = { hpt: headerSet.has(r) ? 24 : 19 };
  }
  ws['!rows'] = rowsMeta;

  for (let r = range.s.r; r <= range.e.r; r++) {
    const isHeader = headerSet.has(r);
    const isBodyBold = !isHeader && boldSet.has(r);
    const alignRight = (c: number) => !isHeader && rightSet.has(c);

    for (let c = range.s.c; c <= range.e.c; c++) {
      const addr = XLSX.utils.encode_cell({ r, c });
      const cell = ws[addr];
      if (!cell) {
        continue;
      }

      const style: CellStyle = {
        font: {
          name: 'Calibri',
          sz: 11,
          bold: isHeader || isBodyBold,
          color: { rgb: isHeader ? HEADER_FONT : 'FF111827' }
        },
        alignment: {
          vertical: 'center',
          horizontal: isHeader ? 'center' : alignRight(c) ? 'right' : 'left',
          wrapText: true
        },
        border: BORDER_THIN
      };

      if (isHeader) {
        style.fill = {
          patternType: 'solid',
          fgColor: { rgb: HEADER_FILL },
          bgColor: { rgb: HEADER_FILL }
        };
      } else if (isBodyBold) {
        style.fill = {
          patternType: 'solid',
          fgColor: { rgb: SECTION_FILL },
          bgColor: { rgb: SECTION_FILL }
        };
      }

      cell.s = style;
    }
  }

  const wb = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(wb, ws, sanitizeSheetName(options.sheetName));
  XLSX.writeFile(wb, ensureExtension(options.fileName), { cellStyles: true });
}

/** Rows where column B is empty but A has text (report section titles) */
export function inferSectionTitleRows(matrix: unknown[][], firstDataRow = 1): number[] {
  const out: number[] = [];
  for (let i = firstDataRow; i < matrix.length; i++) {
    const row = matrix[i];
    if (!row?.length) {
      continue;
    }
    const a = row[0];
    const b = row[1];
    const bEmpty = b === '' || b === null || b === undefined;
    if (bEmpty && a !== '' && a !== null && a !== undefined) {
      out.push(i);
    }
  }
  return out;
}
