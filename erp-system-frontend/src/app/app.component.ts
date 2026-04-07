import { Component, HostListener, OnInit } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import { filter } from 'rxjs/operators';
import { ThemeService } from './core/services/theme.service';
import { CommandPaletteService } from './core/services/command-palette.service';

@Component({ standalone: false,
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {

  constructor(
    private router: Router,
    private themeService: ThemeService,
    private commandPalette: CommandPaletteService
  ) { }

  ngOnInit() {
    this.themeService.init();
    this.syncAuthViewportLock(this.router.url);
    this.router.events
      .pipe(filter((evt): evt is NavigationEnd => evt instanceof NavigationEnd))
      .subscribe((evt) => {
        const url = evt.urlAfterRedirects || evt.url || '';
        this.syncAuthViewportLock(url);
        if (!this.isAuthRouteUrl(url)) {
          window.scrollTo(0, 0);
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
