import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export type ThemeMode = 'light' | 'dark';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly key = 'erp_theme_mode';
  private readonly modeSubject = new BehaviorSubject<ThemeMode>(this.readMode());
  /** Login/auth screens: always show dark UI without persisting or changing user preference. */
  private authRouteDarkOnly = false;

  readonly mode$ = this.modeSubject.asObservable();

  get mode(): ThemeMode {
    return this.modeSubject.value;
  }

  init(): void {
    this.applyEffective();
  }

  /**
   * Call when entering/leaving `/auth` shell so login stays dark while stored mode
   * (e.g. light) applies again after sign-in.
   */
  setAuthRouteDarkOnly(active: boolean): void {
    this.authRouteDarkOnly = active;
    this.applyEffective();
  }

  setMode(mode: ThemeMode): void {
    localStorage.setItem(this.key, mode);
    this.modeSubject.next(mode);
    this.applyEffective();
  }

  toggle(): void {
    this.setMode(this.mode === 'dark' ? 'light' : 'dark');
  }

  /** Alias for enterprise shell / login — same as {@link toggle}. */
  toggleTheme(): void {
    this.toggle();
  }

  /** Current persisted theme. */
  getCurrentTheme(): ThemeMode {
    return this.modeSubject.value;
  }

  private readMode(): ThemeMode {
    return localStorage.getItem(this.key) === 'dark' ? 'dark' : 'light';
  }

  private applyEffective(): void {
    const stored = this.modeSubject.value;
    const visual: ThemeMode = this.authRouteDarkOnly ? 'dark' : stored;
    this.applyToDocument(visual);
  }

  private applyToDocument(mode: ThemeMode): void {
    document.documentElement.setAttribute('data-theme', mode);
    document.documentElement.style.setProperty('color-scheme', mode);
    document.body.classList.toggle('theme-light', mode === 'light');
    document.body.classList.toggle('theme-dark', mode === 'dark');
    document.body.classList.toggle('light-mode', mode === 'light');
    document.body.classList.toggle('dark-mode', mode === 'dark');
    document.body.classList.toggle('flat-able-dark', mode === 'dark');
  }
}
