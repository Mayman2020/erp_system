import { Component } from '@angular/core';

@Component({
  standalone: false,
  selector: 'app-sidebar',
  template: `
    <section class="erp-sidebar-shell">
      <div class="erp-sidebar-shell__profile">
        <ng-content select="[sidebar-profile]"></ng-content>
      </div>
      <div class="erp-sidebar-shell__divider"></div>
      <div class="erp-sidebar-shell__menu">
        <ng-content select="[sidebar-menu]"></ng-content>
      </div>
    </section>
  `
})
export class SidebarComponent {}
