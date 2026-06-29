import { ChangeDetectionStrategy, Component, Input } from '@angular/core';

@Component({
  standalone: false,
  selector: 'app-journal-link',
  template: `
    <a *ngIf="journalEntryId" class="erp-journal-link" [routerLink]="['/journal-entries']" [queryParams]="{ highlight: journalEntryId }">
      {{ 'ERP.JOURNAL_ENTRY' | translate }} #{{ journalEntryId }}
    </a>
  `,
  styles: [`.erp-journal-link { font-size: 0.875rem; }`],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class JournalLinkComponent {
  @Input() journalEntryId?: number | null;
}
