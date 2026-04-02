import { Component, Input } from '@angular/core';
import { TranslationService } from '../../../core/i18n/translation.service';

@Component({ standalone: false,
  selector: 'app-status-badge',
  template: '<span class="erp-badge erp-badge--status" [ngClass]="statusClass">{{ label }}</span>'
})
export class StatusBadgeComponent {
  @Input() status = 'DRAFT';
  @Input() labelPrefix = 'STATUS.';

  constructor(private translationService: TranslationService) {}

  get label(): string {
    const key = `${this.labelPrefix}${this.status}`;
    const translated = this.translationService.instant(key);
    return translated === key ? this.humanize(this.status) : translated;
  }

  get statusClass(): string {
    switch ((this.status || '').toUpperCase()) {
      case 'POSTED':
      case 'ACTIVE':
      case 'MATCHED':
      case 'FINALIZED':
      case 'YES':
        return 'erp-badge--success';
      case 'APPROVED':
      case 'INFO':
        return 'erp-badge--info';
      case 'PARTIALLY_MATCHED':
      case 'PENDING':
      case 'DRAFT':
        return 'erp-badge--warning';
      case 'INACTIVE':
      case 'CANCELLED':
      case 'REVERSED':
      case 'FAILED':
      case 'UNMATCHED':
        return 'erp-badge--danger';
      case 'NO':
      default:
        return 'erp-badge--neutral';
    }
  }

  private humanize(value: string): string {
    return (value || '')
      .toString()
      .replace(/_/g, ' ')
      .toLowerCase()
      .replace(/\b\w/g, (char) => char.toUpperCase());
  }
}
