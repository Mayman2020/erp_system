import { Component, Input } from '@angular/core';
import { TranslationService } from '../../../core/i18n/translation.service';

interface LanguageOption {
  code: string;
  labelKey: string;
  flagSrc: string;
}

@Component({
  standalone: false,
  selector: 'app-language-switcher',
  template: `
    <div class="erp-language-switcher" ngbDropdown [placement]="placement">
      <button
        type="button"
        class="erp-language-switcher__trigger"
        [class.erp-language-switcher__trigger--compact]="compact"
        ngbDropdownToggle
        [attr.aria-label]="ariaLabelKey | translate"
        [attr.title]="ariaLabelKey | translate"
      >
        <img
          [src]="currentLanguageOption.flagSrc"
          [attr.alt]="currentLanguageOption.labelKey | translate"
          class="erp-language-switcher__flag"
        />
        <span *ngIf="!compact" class="erp-language-switcher__label">
          {{ currentLanguageOption.labelKey | translate }}
        </span>
        <span class="sr-only" *ngIf="compact">{{ currentLanguageOption.labelKey | translate }}</span>
        <mat-icon aria-hidden="true" class="erp-language-switcher__caret" *ngIf="!compact">expand_more</mat-icon>
      </button>

      <div
        class="dropdown-menu erp-lang-menu"
        [class.dropdown-menu-right]="align === 'end'"
        ngbDropdownMenu
      >
        <button
          type="button"
          class="dropdown-item erp-lang-menu__item"
          *ngFor="let language of languages"
          [class.is-active]="isActiveLanguage(language.code)"
          (click)="switchLanguage(language.code)"
        >
          <span class="erp-lang-menu__content">
            <img
              [src]="language.flagSrc"
              [attr.alt]="language.labelKey | translate"
              class="erp-lang-menu__flag"
            />
            <span>{{ language.labelKey | translate }}</span>
          </span>
          <mat-icon aria-hidden="true" class="erp-lang-menu__check" *ngIf="isActiveLanguage(language.code)">check</mat-icon>
        </button>
      </div>
    </div>
  `
})
export class LanguageSwitcherComponent {
  @Input() compact = false;
  @Input() placement = 'bottom-right';
  @Input() align: 'start' | 'end' = 'end';
  @Input() ariaLabelKey = 'PROFILE.LANGUAGE';

  readonly languages: LanguageOption[] = [
    { code: 'ar', labelKey: 'LANG.ARABIC', flagSrc: 'assets/images/flags/uae.svg' },
    { code: 'en', labelKey: 'LANG.ENGLISH', flagSrc: 'assets/images/flags/uk.svg' }
  ];

  constructor(private translationService: TranslationService) {}

  get currentLanguageOption(): LanguageOption {
    return this.languages.find((item) => item.code === this.translationService.currentLanguage) || this.languages[0];
  }

  switchLanguage(lang: string): void {
    if (this.translationService.currentLanguage === lang) {
      return;
    }
    this.translationService.setLanguage(lang).subscribe();
  }

  isActiveLanguage(lang: string): boolean {
    return this.translationService.currentLanguage === lang;
  }
}
