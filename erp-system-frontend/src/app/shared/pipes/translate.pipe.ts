import { ChangeDetectorRef, OnDestroy, Pipe, PipeTransform } from '@angular/core';
import { Subscription } from 'rxjs';
import { TranslationService } from '../../core/i18n/translation.service';

abstract class BaseTranslatePipe implements PipeTransform, OnDestroy {
  private key = '';
  private translated = '';
  private readonly subscription: Subscription;

  constructor(private translationService: TranslationService, private cdr: ChangeDetectorRef) {
    this.subscription = this.translationService.currentLanguage$.subscribe(() => {
      if (this.key) {
        this.translated = this.translationService.instant(this.key);
        this.cdr.markForCheck();
      }
    });
  }

  transform(key: string): string {
    if (!key) {
      return '';
    }

    if (this.key !== key) {
      this.key = key;
      this.translated = this.translationService.instant(key);
    }

    return this.translated;
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }
}

@Pipe({
  name: 'translate',
  pure: false
})
export class TranslatePipe extends BaseTranslatePipe {}

@Pipe({
  name: 't',
  pure: false
})
export class LegacyTranslatePipe extends BaseTranslatePipe {}
