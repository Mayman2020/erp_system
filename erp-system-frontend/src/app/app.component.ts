import { Component, HostListener, OnInit } from '@angular/core';
import { NavigationCancel, NavigationEnd, NavigationError, NavigationStart, Router } from '@angular/router';
import { ThemeService } from './core/services/theme.service';
import { CommandPaletteService } from './core/services/command-palette.service';

@Component({ standalone: false,
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {
  /** Thin top bar during lazy route loads (complements full-screen spinner). */
  routeNavigating = false;

  constructor(
    private router: Router,
    private themeService: ThemeService,
    private commandPalette: CommandPaletteService
  ) { }

  ngOnInit() {
    this.themeService.init();
    this.syncAuthViewportLock(this.router.url);
    this.router.events.subscribe((evt) => {
      if (evt instanceof NavigationStart) {
        this.routeNavigating = true;
        return;
      }
      if (evt instanceof NavigationEnd || evt instanceof NavigationCancel || evt instanceof NavigationError) {
        this.routeNavigating = false;
        const url = evt instanceof NavigationEnd ? evt.urlAfterRedirects || evt.url || '' : this.router.url || '';
        this.syncAuthViewportLock(url);
        if (evt instanceof NavigationEnd && !this.isAuthRouteUrl(url)) {
          window.scrollTo(0, 0);
        }
      }
    });
  }

  /** `/auth/...` uses a fixed viewport; skip window scroll to avoid scrollbar flicker. */
  private isAuthRouteUrl(url: string): boolean {
    const path = (url || '').split('?')[0] || '';
    return path === '/auth' || path.startsWith('/auth/');
  }

  private syncAuthViewportLock(url: string): void {
    const on = this.isAuthRouteUrl(url);
    document.documentElement.classList.toggle('erp-auth-html-lock', on);
    document.body.classList.toggle('erp-auth-body-lock', on);
  }

  @HostListener('document:keydown', ['$event'])
  onGlobalKeydown(e: KeyboardEvent): void {
    if (!(e.ctrlKey || e.metaKey) || (e.key !== 'k' && e.key !== 'K')) {
      return;
    }
    const t = e.target as HTMLElement | null;
    if (t && (t.tagName === 'INPUT' || t.tagName === 'TEXTAREA' || t.isContentEditable)) {
      return;
    }
    e.preventDefault();
    const url = this.router.url || '';
    if (this.isAuthRouteUrl(url)) {
      return;
    }
    this.commandPalette.toggle();
  }
}
