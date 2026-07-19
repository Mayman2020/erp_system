import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { DateFormatService } from '../../../core/services/date-format.service';

@Component({
  standalone: false,
  selector: 'app-erp-audit-trail',
  template: `
    <div class="erp-audit-trail" *ngIf="hasAny">
      <div class="erp-audit-trail__title">
        <mat-icon aria-hidden="true">history</mat-icon>
        <span>{{ 'AUDIT.INFO_TITLE' | translate }}</span>
      </div>
      <div class="erp-audit-trail__grid">
        <div class="erp-audit-trail__cell" *ngIf="showCreatedRow">
          <span class="erp-audit-trail__label">{{ 'AUDIT.CREATED_BY' | translate }}</span>
          <span class="erp-audit-trail__value">{{ createdByLabel }}</span>
        </div>
        <div class="erp-audit-trail__cell" *ngIf="showCreatedRow">
          <span class="erp-audit-trail__label">{{ 'AUDIT.CREATED_AT' | translate }}</span>
          <span class="erp-audit-trail__value">{{ createdAtLabel }}</span>
        </div>
        <div class="erp-audit-trail__cell" *ngIf="showModifiedRow">
          <span class="erp-audit-trail__label">{{ 'AUDIT.MODIFIED_BY' | translate }}</span>
          <span class="erp-audit-trail__value">{{ modifiedByLabel }}</span>
        </div>
        <div class="erp-audit-trail__cell" *ngIf="showModifiedRow">
          <span class="erp-audit-trail__label">{{ 'AUDIT.UPDATED_AT' | translate }}</span>
          <span class="erp-audit-trail__value">{{ updatedAtLabel }}</span>
        </div>
        <div class="erp-audit-trail__cell erp-audit-trail__cell--wide" *ngIf="approvedByName || approvedBy">
          <span class="erp-audit-trail__label">{{ 'AUDIT.APPROVED_BY' | translate }}</span>
          <span class="erp-audit-trail__value">{{ approvedByName || ('AUDIT.UNKNOWN' | translate) }}</span>
        </div>
      </div>
    </div>
  `,
  styles: [`
    :host { display: block; width: 100%; }
    .erp-audit-trail {
      margin-top: var(--space-3, 12px);
      padding: var(--space-3, 12px) var(--space-4, 14px);
      border-radius: var(--radius-md, 8px);
      background: var(--bg-muted, var(--erp-bg-muted));
      border: 1px solid var(--border-color, var(--erp-border));
    }
    .erp-audit-trail__title {
      display: flex;
      align-items: center;
      gap: var(--space-2, 8px);
      font-weight: 600;
      margin-bottom: 10px;
      color: var(--text-primary, var(--erp-text));
      font-size: var(--text-body, 14px);
    }
    .erp-audit-trail__title mat-icon { font-size: 18px; width: 18px; height: 18px; color: var(--color-primary, var(--erp-info)); }
    .erp-audit-trail__grid {
      display: grid;
      grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
      column-gap: 24px;
      row-gap: 10px;
    }
    .erp-audit-trail__cell { display: flex; flex-direction: column; gap: 3px; min-width: 0; }
    .erp-audit-trail__cell--wide { grid-column: 1 / -1; }
    .erp-audit-trail__label { font-size: var(--text-caption, 12px); color: var(--text-muted, var(--erp-text-soft)); }
    .erp-audit-trail__value { font-size: 13px; font-weight: 500; color: var(--text-primary, var(--erp-text)); word-break: break-word; }
    @media (max-width: 520px) {
      .erp-audit-trail__grid { grid-template-columns: 1fr; }
      .erp-audit-trail__cell--wide { grid-column: auto; }
    }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ErpAuditTrailComponent {
  @Input() createdAt?: string | null;
  @Input() updatedAt?: string | null;
  @Input() createdBy?: string | number | null;
  @Input() createdByName?: string | null;
  @Input() modifiedBy?: string | number | null;
  @Input() modifiedByName?: string | null;
  @Input() updatedBy?: string | number | null;
  @Input() approvedBy?: number | null;
  @Input() approvedByName?: string | null;

  constructor(private readonly dateFormat: DateFormatService) {}

  get showCreatedRow(): boolean {
    return !!(this.createdAt || this.createdByName || this.createdBy);
  }

  get showModifiedRow(): boolean {
    return !!(this.updatedAt || this.modifiedByName || this.modifiedBy || this.updatedBy);
  }

  get createdByLabel(): string {
    const name = (this.createdByName ?? '').trim();
    if (name) return name;
    if (this.createdBy != null && String(this.createdBy).trim()) return String(this.createdBy);
    return '—';
  }

  get modifiedByLabel(): string {
    const name = (this.modifiedByName ?? '').trim();
    if (name) return name;
    if (this.modifiedBy != null && String(this.modifiedBy).trim()) return String(this.modifiedBy);
    if (this.updatedBy != null && String(this.updatedBy).trim()) return String(this.updatedBy);
    return '—';
  }

  get createdAtLabel(): string {
    return this.createdAt ? this.formatDate(this.createdAt) : '—';
  }

  get updatedAtLabel(): string {
    return this.updatedAt ? this.formatDate(this.updatedAt) : '—';
  }

  get hasAny(): boolean {
    return this.showCreatedRow || this.showModifiedRow || !!(this.approvedByName || this.approvedBy);
  }

  private formatDate(value: string): string {
    return this.dateFormat.format(value) || value;
  }
}
