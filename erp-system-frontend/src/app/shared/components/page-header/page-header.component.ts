import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-page-header',
  template: `
    <section class="erp-page-header">
      <div class="erp-page-header__copy">
        <span class="erp-page-header__eyebrow" *ngIf="eyebrowKey">{{ eyebrowKey | translate }}</span>
        <div class="erp-page-header__title-row">
          <h1>{{ titleKey | translate }}</h1>
          <ng-content select="[header-meta]"></ng-content>
        </div>
        <p *ngIf="subtitleKey">{{ subtitleKey | translate }}</p>
      </div>
      <div class="erp-page-header__actions">
        <ng-content select="[header-actions]"></ng-content>
      </div>
    </section>
  `
})
export class PageHeaderComponent {
  @Input() titleKey = '';
  @Input() subtitleKey = '';
  @Input() eyebrowKey = '';
}
