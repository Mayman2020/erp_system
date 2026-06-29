import { animate, style, transition, trigger } from '@angular/animations';
import { Component, HostListener, NgZone, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { NextConfig } from '../../../app-config';
import { Location } from '@angular/common';

@Component({ standalone: false,
  selector: 'app-admin',
  templateUrl: './admin.component.html',
  styleUrls: ['./admin.component.scss'],
  animations: [
    trigger('erpPageTransition', [
      transition('* => *', [
        style({ opacity: 0, transform: 'translateY(8px)' }),
        animate('280ms cubic-bezier(0.22, 1, 0.36, 1)', style({ opacity: 1, transform: 'none' }))
      ])
    ])
  ]
})
export class AdminComponent implements OnInit {
  public flatConfig: any;
  public navCollapsed: boolean;
  /** Desktop: hide the vertical sidebar completely (hamburger). */
  public navSidebarHidden = false;
  public navCollapsedMob: boolean;
  public windowWidth: number;

  constructor(
    private zone: NgZone,
    private location: Location,
    public router: Router
  ) {
    this.flatConfig = NextConfig.config;
    let currentURL = this.location.path();
    const baseHerf = this.location['_baseHref'];
    if (baseHerf) {
      currentURL = baseHerf + this.location.path();
    }

    this.windowWidth = window.innerWidth;

    if (currentURL === baseHerf + '/layout/collapse-menu'
      || currentURL === baseHerf + '/layout/box') {
      this.flatConfig.collapseMenu = true;
    }

    this.navCollapsed = (this.windowWidth >= 992) ? this.flatConfig.collapseMenu : false;
    this.navCollapsedMob = false;

  }

  ngOnInit() {
    // Estate shell: vertical layout only; no PCoded menu positioning hacks.
  }

  /** Desktop: toggle narrow icon-only sidebar (labels hidden; tooltips show titles). */
  toggleSidebarCollapsed(): void {
    if (this.windowWidth < 992) {
      return;
    }
    this.navSidebarHidden = false;
    this.navCollapsed = !this.navCollapsed;
    this.flatConfig.collapseMenu = this.navCollapsed;
  }

  @HostListener('document:keydown.escape')
  onEscapeCloseMobileNav(): void {
    if (window.innerWidth < 992 && this.navCollapsedMob) {
      this.navCollapsedMob = false;
    }
  }

  @HostListener('window:resize')
  onWindowResize(): void {
    if (window.innerWidth >= 992 && this.navCollapsedMob) {
      this.navCollapsedMob = false;
    }
  }

  closeMobileNav(): void {
    if (window.innerWidth < 992) {
      this.navCollapsedMob = false;
    }
  }

  navMobClick() {
    if (this.windowWidth < 992) {
      this.navCollapsedMob = !this.navCollapsedMob;
    }
  }

}
