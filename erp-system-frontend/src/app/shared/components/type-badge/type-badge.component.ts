import { Component, Input } from '@angular/core';
import { TranslationService } from '../../../core/i18n/translation.service';

@Component({ standalone: false,
  selector: 'app-type-badge',
  template: '<span class="erp-badge erp-badge--type" [ngClass]="typeClass">{{ label }}</span>'
})
export class TypeBadgeComponent {
  @Input() type = '';
  @Input() labelPrefix = '';

  constructor(private translationService: TranslationService) {}

  get label(): string {
    if (!this.labelPrefix) {
      return this.humanize(this.type);
    }
    const key = `${this.labelPrefix}${this.type}`;
    const translated = this.translationService.instant(key);
    return translated === key ? this.humanize(this.type) : translated;
  }

  get typeClass(): string {
    if (this.labelPrefix === 'PAYMENT_METHOD.') {
      return 'erp-badge--neutral';
    }
    switch ((this.type || '').toUpperCase()) {
      case 'ASSET':
      case 'CASH':
      case 'BANK':
      case 'STANDARD':
        return 'erp-badge--info';
      case 'LIABILITY':
      case 'CHECK':
      case 'ADVANCE':
        return 'erp-badge--warning';
      case 'EQUITY':
      case 'REVENUE':
      case 'INVOICE_COLLECTION':
        return 'erp-badge--success';
      case 'EXPENSE':
      case 'BILL_PAYMENT':
        return 'erp-badge--danger';
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
