import { animate, style, transition, trigger } from '@angular/animations';
import { Component, NgZone, OnInit } from '@angular/core';
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
        style({ opacity: 0, transform: 'translateY(10px)' }),
        animate('260ms cubic-bezier(0.33, 1, 0.68, 1)', style({ opacity: 1, transform: 'none' }))
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
    if (this.windowWidth < 992) {
      this.flatConfig.layout = 'vertical';
      setTimeout(() => {
        document.querySelector('.pcoded-navbar').classList.add('menupos-static');
        (document.querySelector('#nav-ps-flat-able') as HTMLElement).style.maxHeight = '100%'; // 100% amit
      }, 500);
    }
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

  navMobClick() {
    if (this.windowWidth < 992) {
      if (this.navCollapsedMob && !(document.querySelector('app-navigation.pcoded-navbar').classList.contains('mob-open'))) {
        this.navCollapsedMob = !this.navCollapsedMob;
        setTimeout(() => {
          this.navCollapsedMob = !this.navCollapsedMob;
        }, 100);
      } else {
        this.navCollapsedMob = !this.navCollapsedMob;
      }
    }
  }

}
