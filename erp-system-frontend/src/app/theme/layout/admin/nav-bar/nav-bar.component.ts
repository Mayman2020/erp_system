import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { NextConfig } from '../../../../app-config';

@Component({
  standalone: false,
  selector: 'app-nav-bar',
  templateUrl: './nav-bar.component.html',
  styleUrls: ['./nav-bar.component.scss']
})
export class NavBarComponent implements OnInit {
  public flatConfig: any;
  public windowWidth: number;

  @Output() onNavCollapse = new EventEmitter();
  @Output() onNavHeaderMobCollapse = new EventEmitter();

  constructor() {
    this.flatConfig = NextConfig.config;
    this.windowWidth = window.innerWidth;
  }

  ngOnInit(): void {
    this.windowWidth = window.innerWidth;
  }

  navCollapse(): void {
    if (this.windowWidth >= 992) {
      this.onNavCollapse.emit();
    } else {
      this.onNavHeaderMobCollapse.emit();
    }
  }
}
