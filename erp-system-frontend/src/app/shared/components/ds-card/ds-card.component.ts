import { Component } from '@angular/core';

/** Semantic surface wrapper; uses global `.erp-card` design tokens. */
@Component({
  standalone: false,
  selector: 'app-card',
  template: '<ng-content></ng-content>',
  host: { class: 'erp-card' }
})
export class DsCardComponent {}
