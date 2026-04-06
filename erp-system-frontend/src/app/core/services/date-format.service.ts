import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class DateFormatService {
  readonly placeholder = 'dd/MM/yyyy';

  format(value: unknown): string {
    const parts = this.extractParts(value);
    if (!parts) {
      return typeof value === 'string' ? value : '';
    }
    return `${parts.day}/${parts.month}/${parts.year}`;
  }

  toIsoDate(value: unknown): string {
    const parts = this.extractParts(value);
    if (!parts) {
      return '';
    }
    return `${parts.year}-${parts.month}-${parts.day}`;
  }

  isDateKey(key: string): boolean {
    const k = (key || '').trim();
    if (!k) {
      return false;
    }
    const lower = k.toLowerCase();
    if (lower === 'date') {
      return true;
    }
    if (lower.endsWith('date')) {
      return true;
    }
    /* camelCase timestamps: createdAt, postedAt, reversedAt, … */
    return /[a-z]At$/i.test(k);
  }

  normalizeDisplayInput(raw: string): string {
    const digits = (raw || '').replace(/\D/g, '').slice(0, 8);
    if (digits.length <= 2) {
      return digits;
    }
    if (digits.length <= 4) {
      return `${digits.slice(0, 2)}/${digits.slice(2)}`;
    }
    return `${digits.slice(0, 2)}/${digits.slice(2, 4)}/${digits.slice(4)}`;
  }

  isValidDisplayDate(raw: string): boolean {
    return !!this.extractParts(raw);
  }

  private extractParts(value: unknown): { day: string; month: string; year: string } | null {
    if (value === null || value === undefined || value === '') {
      return null;
    }

    if (value instanceof Date && !Number.isNaN(value.getTime())) {
      return this.fromDate(value);
    }

    if (typeof value !== 'string') {
      return null;
    }

    const trimmed = value.trim();
    if (!trimmed) {
      return null;
    }

    const isoMatch = trimmed.match(/^(\d{4})-(\d{2})-(\d{2})(?:[T\s].*)?$/);
    if (isoMatch) {
      return this.normalizeParts(isoMatch[3], isoMatch[2], isoMatch[1]);
    }

    const slashMatch = trimmed.match(/^(\d{2})\/(\d{2})\/(\d{4})$/);
    if (slashMatch) {
      return this.normalizeParts(slashMatch[1], slashMatch[2], slashMatch[3]);
    }

    const dashMatch = trimmed.match(/^(\d{2})-(\d{2})-(\d{4})$/);
    if (dashMatch) {
      return this.normalizeParts(dashMatch[1], dashMatch[2], dashMatch[3]);
    }

    const parsed = new Date(trimmed);
    if (!Number.isNaN(parsed.getTime())) {
      return this.fromDate(parsed);
    }

    return null;
  }

  private fromDate(value: Date): { day: string; month: string; year: string } {
    return this.normalizeParts(
      `${value.getDate()}`.padStart(2, '0'),
      `${value.getMonth() + 1}`.padStart(2, '0'),
      `${value.getFullYear()}`
    )!;
  }

  private normalizeParts(day: string, month: string, year: string): { day: string; month: string; year: string } | null {
    const dayNumber = Number(day);
    const monthNumber = Number(month);
    const yearNumber = Number(year);

    if (!Number.isInteger(dayNumber) || !Number.isInteger(monthNumber) || !Number.isInteger(yearNumber)) {
      return null;
    }

    if (yearNumber < 1000 || monthNumber < 1 || monthNumber > 12) {
      return null;
    }

    const maxDay = new Date(yearNumber, monthNumber, 0).getDate();
    if (dayNumber < 1 || dayNumber > maxDay) {
      return null;
    }

    return {
      day: `${dayNumber}`.padStart(2, '0'),
      month: `${monthNumber}`.padStart(2, '0'),
      year: `${yearNumber}`.padStart(4, '0')
    };
  }
}
