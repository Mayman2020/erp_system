import { Component, OnDestroy, OnInit } from '@angular/core';
import { NgbDropdownConfig } from '@ng-bootstrap/ng-bootstrap';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { TranslationService } from '../../../../../core/i18n/translation.service';
import { ThemeService } from '../../../../../core/services/theme.service';

@Component({
  selector: 'app-nav-right',
  templateUrl: './nav-right.component.html',
  styleUrls: ['./nav-right.component.scss'],
  providers: [NgbDropdownConfig]
})
export class NavRightComponent implements OnInit, OnDestroy {
  darkMode = false;
  readonly languages = [
    { code: 'ar', labelKey: 'LANG.ARABIC', flagSrc: 'assets/images/flags/uae.svg' },
    { code: 'en', labelKey: 'LANG.ENGLISH', flagSrc: 'assets/images/flags/uk.svg' }
  ];

  private readonly destroy$ = new Subject<void>();

  constructor(
    private themeService: ThemeService,
    private translationService: TranslationService
  ) {}

  ngOnInit(): void {
    this.darkMode = this.themeService.mode === 'dark';
    this.themeService.mode$.pipe(takeUntil(this.destroy$)).subscribe((m) => {
      this.darkMode = m === 'dark';
    });
  }

  toggleTheme(): void {
    this.themeService.toggle();
  }

  switchLanguage(lang: string): void {
    this.translationService.setLanguage(lang).subscribe();
  }

  isActiveLanguage(lang: string): boolean {
    return this.translationService.currentLanguage === lang;
  }

  currentLanguageOption(): { code: string; labelKey: string; flagSrc: string } {
    return this.languages.find((item) => item.code === this.translationService.currentLanguage) || this.languages[0];
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
