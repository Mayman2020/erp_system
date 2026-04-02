import { Component, EventEmitter, OnDestroy, OnInit, Output } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { NextConfig } from '../../../../app-config';
import { ThemeService, ThemeMode } from '../../../../core/services/theme.service';

@Component({
  selector: 'app-nav-bar',
  templateUrl: './nav-bar.component.html',
  styleUrls: ['./nav-bar.component.scss']
})
export class NavBarComponent implements OnInit, OnDestroy {
  public flatConfig: any;
  public menuClass: boolean;
  public collapseStyle: string;
  public windowWidth: number;

  logoFullSrc = 'assets/images/brand/coreerp-logo-light.svg';
  logoMarkSrc = 'assets/images/brand/coreerp-mark-light.svg';

  @Output() onNavCollapse = new EventEmitter();
  @Output() onNavHeaderMobCollapse = new EventEmitter();

  private readonly destroy$ = new Subject<void>();

  constructor(private themeService: ThemeService) {
    this.flatConfig = NextConfig.config;
    this.menuClass = false;
    this.collapseStyle = 'none';
    this.windowWidth = window.innerWidth;
  }

  ngOnInit(): void {
    this.applyTheme(this.themeService.mode);
    this.themeService.mode$.pipe(takeUntil(this.destroy$)).subscribe((m) => this.applyTheme(m));
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private applyTheme(mode: ThemeMode): void {
    if (mode === 'dark') {
      this.logoFullSrc = 'assets/images/brand/coreerp-logo-dark.svg';
      this.logoMarkSrc = 'assets/images/brand/coreerp-mark-dark.svg';
    } else {
      this.logoFullSrc = 'assets/images/brand/coreerp-logo-light.svg';
      this.logoMarkSrc = 'assets/images/brand/coreerp-mark-light.svg';
    }
  }

  toggleMobOption() {
    this.menuClass = !this.menuClass;
    this.collapseStyle = (this.menuClass) ? 'block' : 'none';
  }

  navCollapse() {
    if (this.windowWidth >= 992) {
      this.onNavCollapse.emit();
    } else {
      this.onNavHeaderMobCollapse.emit();
    }
  }

}
