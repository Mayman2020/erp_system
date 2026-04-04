import { Component } from '@angular/core';

@Component({
  standalone: false,
  selector: 'app-tabs',
  template: `
    <section class="erp-tabs">
      <ng-content></ng-content>
    </section>
  `
})
export class TabsComponent {}
