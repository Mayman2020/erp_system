import { booleanAttribute, Component, HostBinding, Input, OnDestroy, OnInit } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { environment } from '../../../../../environments/environment';
import type { AppEnvironment } from '../../../../../environments/environment.types';
import { TranslationService } from '../../../../core/i18n/translation.service';

@Component({
  standalone: false,
  selector: 'app-shell-footer',
  templateUrl: './shell-footer.component.html',
  styleUrls: ['./shell-footer.component.scss']
})
export class ShellFooterComponent implements OnInit, OnDestroy {
  /** Tight layout for auth/login so the page fits one viewport without body scroll */
  @Input({ transform: booleanAttribute }) compact = false;

  @HostBinding('class.erp-shell-footer--compact')
  get compactHostClass(): boolean {
    return this.compact;
  }
  readonly year = new Date().getFullYear();
  readonly version = (environment as Partial<AppEnvironment>).appVersion ?? '1.0.0';
  readonly envLabelKey = environment.production ? 'APP.ENV_PRODUCTION' : 'APP.ENV_LOCAL';
  footerIntegratedText = '';
  private readonly destroy$ = new Subject<void>();

  constructor(private i18n: TranslationService) {}

  ngOnInit(): void {
    this.refreshFooterText();
    this.i18n.currentLanguage$.pipe(takeUntil(this.destroy$)).subscribe(() => this.refreshFooterText());
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private refreshFooterText(): void {
    this.footerIntegratedText = this.i18n
      .instant('APP.FOOTER_INTEGRATED')
      .replace('{{year}}', String(this.year));
  }
}
