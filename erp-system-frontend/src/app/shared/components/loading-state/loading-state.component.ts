import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-loading-state',
  template: `
    <div class="erp-loading-state" [class.erp-loading-state--compact]="compact">
      <div class="erp-loading-state__spinner" aria-hidden="true">
        <mat-icon>autorenew</mat-icon>
      </div>
      <div class="erp-loading-state__content">
        <h3>{{ titleKey | translate }}</h3>
        <p *ngIf="descriptionKey">{{ descriptionKey | translate }}</p>
      </div>
    </div>
  `
})
export class LoadingStateComponent {
  @Input() titleKey = 'COMMON.LOADING';
  @Input() descriptionKey = 'COMMON.PLEASE_WAIT';
  @Input() compact = false;
}
