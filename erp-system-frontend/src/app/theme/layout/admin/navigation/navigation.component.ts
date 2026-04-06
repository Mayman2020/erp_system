import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { NextConfig } from '../../../../app-config';

@Component({
  standalone: false,
  selector: 'app-navigation',
  templateUrl: './navigation.component.html',
  styleUrls: ['./navigation.component.scss']
})
export class NavigationComponent implements OnInit {
  public windowWidth: number;
  public flatConfig: any;
  /** Synced from admin: true = icon-only sidebar */
  @Input() sidebarCollapsed = false;
  @Output() onNavCollapse = new EventEmitter();
  @Output() onNavMobCollapse = new EventEmitter();

  constructor() {
    this.flatConfig = NextConfig.config;
    this.windowWidth = window.innerWidth;
  }

  ngOnInit() { }

  navCollapse() {
    if (this.windowWidth >= 992) {
      this.onNavCollapse.emit();
    } else {
      this.onNavMobCollapse.emit();
    }
  }

  navMobCollapse() {
    if (this.windowWidth < 992) {
      this.onNavMobCollapse.emit();
    }
  }
}
