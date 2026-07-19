import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { AuthService, AuthUser, resolveProfileFullName } from '../../../../../core/auth/auth.service';
import { ThemeService } from '../../../../../core/services/theme.service';
import { TranslationService } from '../../../../../core/i18n/translation.service';
import { NotificationService } from '../../../../../core/services/notification.service';

@Component({
  standalone: false,
  selector: 'app-nav-right',
  templateUrl: './nav-right.component.html',
  styleUrls: ['./nav-right.component.scss']
})
export class NavRightComponent implements OnInit, OnDestroy {
  darkMode = false;
  unreadCount = 0;
  currentLang = 'ar';
  now = new Date();

  readonly authenticated$ = this.authService.isAuthenticated$;
  readonly user$ = this.authService.currentUser$;
  private readonly destroy$ = new Subject<void>();
  private pollTimer?: ReturnType<typeof setInterval>;
  private clockTimer?: ReturnType<typeof setInterval>;

  get clockText(): string {
    return new Intl.DateTimeFormat(this.currentLang === 'ar' ? 'ar-u-nu-latn' : 'en-GB', {
      weekday: 'short', year: 'numeric', month: '2-digit', day: '2-digit',
      hour: '2-digit', minute: '2-digit', second: '2-digit'
    }).format(this.now);
  }

  constructor(
    private readonly themeService: ThemeService,
    private readonly authService: AuthService,
    private readonly router: Router,
    private readonly cdr: ChangeDetectorRef,
    private readonly translationService: TranslationService,
    private readonly notificationService: NotificationService
  ) {}

  get activeFlagSrc(): string {
    return this.currentLang === 'en' ? 'assets/images/flags/uk.svg' : 'assets/images/flags/uae.svg';
  }

  ngOnInit(): void {
    this.authService.refreshCurrentUser();
    this.currentLang = this.translationService.currentLanguage;
    this.darkMode = this.themeService.mode === 'dark';
    this.themeService.mode$.pipe(takeUntil(this.destroy$)).subscribe((m) => {
      this.darkMode = m === 'dark';
      this.cdr.markForCheck();
    });
    this.translationService.currentLanguage$.pipe(takeUntil(this.destroy$)).subscribe((lang) => {
      this.currentLang = lang;
      this.cdr.markForCheck();
    });
    this.refreshUnread();
    this.pollTimer = setInterval(() => this.refreshUnread(), 45000);
    this.clockTimer = setInterval(() => { this.now = new Date(); this.cdr.markForCheck(); }, 1000);
    this.authenticated$.pipe(takeUntil(this.destroy$)).subscribe((ok) => {
      if (ok) {
        this.refreshUnread();
      } else {
        this.unreadCount = 0;
        this.cdr.markForCheck();
      }
    });
  }

  refreshUnread(): void {
    if (!this.authService.token) {
      return;
    }
    this.notificationService.unreadCount().subscribe({
      next: (count) => {
        this.unreadCount = count;
        this.cdr.markForCheck();
      },
      error: () => undefined
    });
  }

  switchLang(lang: string): void {
    this.translationService.setLanguage(lang).subscribe();
  }

  toggleTheme(): void {
    this.themeService.toggleTheme();
  }

  logout(): void {
    this.authService.logout();
    void this.router.navigate(['/auth/signin']);
  }

  resolveDisplayName(user: AuthUser | null): string {
    if (!user) return '';
    const lang = this.currentLang === 'en' ? 'en' : 'ar';
    const name = resolveProfileFullName(user.profile, lang);
    return name || user.username || user.email || '';
  }

  resolveRoleKey(user: AuthUser | null): string {
    if (!user) return 'PROFILE.TITLE';
    const role = (user.role || '').toUpperCase();
    return role === 'ADMIN' ? 'AUTH.ROLE_ADMIN' : role === 'ACCOUNTANT' ? 'AUTH.ROLE_ACCOUNTANT' : 'PROFILE.TITLE';
  }

  resolveAvatarUrl(user: AuthUser | null): string {
    const image = (user?.profile?.profileImage || '').trim();
    return image;
  }

  resolveInitials(user: AuthUser | null): string {
    const name = this.resolveDisplayName(user).trim();
    if (!name) return 'U';
    const parts = name.split(/\s+/).filter(Boolean);
    if (parts.length === 1) return parts[0].slice(0, 1).toUpperCase();
    return (parts[0].slice(0, 1) + parts[1].slice(0, 1)).toUpperCase();
  }

  ngOnDestroy(): void {
    if (this.pollTimer) {
      clearInterval(this.pollTimer);
    }
    if (this.clockTimer) clearInterval(this.clockTimer);
    this.destroy$.next();
    this.destroy$.complete();
  }
}
