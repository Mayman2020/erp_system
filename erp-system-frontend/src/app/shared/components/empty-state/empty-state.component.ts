import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({ standalone: false,
  selector: 'app-empty-state',
  template: `
    <div class="erp-empty-state">
      <div class="erp-empty-state__icon">
        <mat-icon aria-hidden="true">{{ icon }}</mat-icon>
      </div>
      <h3>{{ titleKey | translate }}</h3>
      <p *ngIf="descriptionKey">{{ descriptionKey | translate }}</p>
      <button *ngIf="actionLabelKey" type="button" class="erp-button erp-button--primary" (click)="action.emit()">
        {{ actionLabelKey | translate }}
      </button>
    </div>
  `
})
export class EmptyStateComponent {
  @Input() icon = 'inbox';
  @Input() titleKey = 'COMMON.NO_DATA';
  @Input() descriptionKey = 'COMMON.NO_RESULTS_HINT';
  @Input() actionLabelKey = '';
  @Output() action = new EventEmitter<void>();
}
