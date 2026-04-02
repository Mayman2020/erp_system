import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { tap } from 'rxjs/operators';
import { NextConfig } from '../../app-config';

@Injectable({ providedIn: 'root' })
export class TranslationService {
  private readonly key = 'erp_language';
  private readonly languageSubject = new BehaviorSubject<string>(this.readLanguage());
  private readonly dictionaries: { [lang: string]: any } = {};

  constructor(private http: HttpClient) {
    this.setLanguage(this.languageSubject.value).subscribe();
  }

  get currentLanguage$(): Observable<string> {
    return this.languageSubject.asObservable();
  }

  get currentLanguage(): string {
    return this.languageSubject.value;
  }

  instant(key: string): string {
    const dictionary = this.dictionaries[this.languageSubject.value] || {};
    const result = key.split('.').reduce((acc: any, part: string) => (acc && acc[part] !== undefined ? acc[part] : null), dictionary);
    return result || key;
  }

  setLanguage(lang: string): Observable<any> {
    localStorage.setItem(this.key, lang);

    if (this.dictionaries[lang]) {
      this.applyDirection(lang);
      this.languageSubject.next(lang);
      return of(this.dictionaries[lang]);
    }

    return this.http.get(`assets/i18n/${lang}.json`).pipe(
      tap((dictionary) => {
        this.dictionaries[lang] = dictionary;
        this.applyDirection(lang);
        this.languageSubject.next(lang);
      })
    );
  }

  private applyDirection(lang: string): void {
    const rtlEnabled = lang === 'ar';
    document.documentElement.setAttribute('lang', lang);
    document.documentElement.setAttribute('dir', rtlEnabled ? 'rtl' : 'ltr');
    document.body.classList.toggle('rtl', rtlEnabled);
    document.body.classList.toggle('lang-ar', rtlEnabled);
    document.body.classList.toggle('lang-en', !rtlEnabled);
    NextConfig.config.rtlLayout = rtlEnabled;
  }

  private readLanguage(): string {
    return localStorage.getItem(this.key) === 'en' ? 'en' : 'ar';
  }
}
