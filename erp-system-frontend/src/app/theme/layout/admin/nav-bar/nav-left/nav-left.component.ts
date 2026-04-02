import { Component, OnInit } from '@angular/core';
import {NextConfig} from '../../../../../app-config';
import { TranslationService } from '../../../../../core/i18n/translation.service';

@Component({
  selector: 'app-nav-left',
  templateUrl: './nav-left.component.html',
  styleUrls: ['./nav-left.component.scss']
})
export class NavLeftComponent implements OnInit {
  public flatConfig: any;
  readonly languages = [
    { code: 'ar', labelKey: 'LANG.ARABIC', flagSrc: 'assets/images/flags/uae.svg' },
    { code: 'en', labelKey: 'LANG.ENGLISH', flagSrc: 'assets/images/flags/uk.svg' }
  ];

  constructor(private translationService: TranslationService) {
    this.flatConfig = NextConfig.config;
  }

  ngOnInit() {
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

}
