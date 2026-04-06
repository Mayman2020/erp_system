import { ChangeDetectorRef, Directive, OnDestroy, Pipe, PipeTransform } from '@angular/core';
import { Subscription } from 'rxjs';
import { TranslationService } from '../../core/i18n/translation.service';

@Directive()
abstract class BaseTranslatePipe implements PipeTransform, OnDestroy {
  private key = '';
  private params: any = null;
  private translated = '';
  private readonly subscription: Subscription;

  constructor(private translationService: TranslationService, private cdr: ChangeDetectorRef) {
    this.subscription = this.translationService.currentLanguage$.subscribe(() => {
      if (this.key) {
        this.translated = this.translate(this.key, this.params);
        this.cdr.markForCheck();
      }
    });
  }

  transform(key: string, params: any = null): string {
    if (!key) {
      return '';
    }

    if (this.key !== key || JSON.stringify(this.params) !== JSON.stringify(params)) {
      this.key = key;
      this.params = params;
      this.translated = this.translate(key, params);
    }

    return this.translated;
  }

  private translate(key: string, params: any): string {
    let result = this.translationService.instant(key);
    if (!params) return result;

    Object.keys(params).forEach((k) => {
      result = result.replace(new RegExp(`{{${k}}}`, 'g'), params[k]);
    });
    return result;
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }
}

@Pipe({ standalone: false,
  name: 'translate',
  pure: false
})
export class TranslatePipe extends BaseTranslatePipe {}

@Pipe({ standalone: false,
  name: 't',
  pure: false
})
export class LegacyTranslatePipe extends BaseTranslatePipe {}
