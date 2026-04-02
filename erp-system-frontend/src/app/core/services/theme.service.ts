import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export type ThemeMode = 'light' | 'dark';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly key = 'erp_theme_mode';
  private readonly modeSubject = new BehaviorSubject<ThemeMode>(this.readMode());

  readonly mode$ = this.modeSubject.asObservable();

  get mode(): ThemeMode {
    return this.modeSubject.value;
  }

  init(): void {
    this.apply(this.modeSubject.value);
  }

  setMode(mode: ThemeMode): void {
    localStorage.setItem(this.key, mode);
    this.modeSubject.next(mode);
    this.apply(mode);
  }

  toggle(): void {
    this.setMode(this.mode === 'dark' ? 'light' : 'dark');
  }

  private readMode(): ThemeMode {
    return localStorage.getItem(this.key) === 'dark' ? 'dark' : 'light';
  }

  private apply(mode: ThemeMode): void {
    document.documentElement.setAttribute('data-theme', mode);
    document.documentElement.style.setProperty('color-scheme', mode);
    document.body.classList.toggle('theme-light', mode === 'light');
    document.body.classList.toggle('theme-dark', mode === 'dark');
    document.body.classList.toggle('flat-able-dark', mode === 'dark');
  }
}
