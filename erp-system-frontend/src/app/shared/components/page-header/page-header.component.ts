import { Component, Input } from '@angular/core';

@Component({ standalone: false,
  selector: 'app-page-header',
  template: `
    <header class="app-page-header erp-page-header" role="banner">
      <div class="page-heading erp-page-header__copy">
        <span class="app-page-eyebrow erp-page-header__eyebrow" *ngIf="eyebrowKey">{{ eyebrowKey | translate }}</span>
        <div class="erp-page-header__title-row">
          <h1 class="app-page-title">{{ titleKey | translate }}</h1>
          <ng-content select="[header-meta]"></ng-content>
        </div>
        <p class="app-page-subtitle" *ngIf="subtitleKey">{{ subtitleKey | translate }}</p>
      </div>
      <div class="page-actions erp-page-header__actions">
        <ng-content select="[header-actions]"></ng-content>
      </div>
    </header>
  `
})
export class PageHeaderComponent {
  @Input() titleKey = '';
  @Input() subtitleKey = '';
  @Input() eyebrowKey = '';
}
