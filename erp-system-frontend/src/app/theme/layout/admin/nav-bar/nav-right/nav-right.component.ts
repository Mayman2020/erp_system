import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { AuthService, AuthUser, resolveProfileFullName } from '../../../../../core/auth/auth.service';
import { ThemeService } from '../../../../../core/services/theme.service';
import { CommandPaletteService } from '../../../../../core/services/command-palette.service';
import { TranslationService } from '../../../../../core/i18n/translation.service';

@Component({ standalone: false,
  selector: 'app-nav-right',
  templateUrl: './nav-right.component.html',
  styleUrls: ['./nav-right.component.scss']
})
export class NavRightComponent implements OnInit, OnDestroy {
  darkMode = false;
  public profileMenuOpen = false;

  /** Show header profile whenever a session exists (token), not only after /profile/me succeeds. */
  public authenticated$ = this.authService.isAuthenticated$;
  public user$ = this.authService.currentUser$;
  public loadingProfile$ = this.authService.loadingUser$;
  private readonly destroy$ = new Subject<void>();

  constructor(
    private themeService: ThemeService,
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef,
    private translationService: TranslationService,
    private commandPalette: CommandPaletteService
  ) {}

  ngOnInit(): void {
    this.authService.refreshCurrentUser();
    this.darkMode = this.themeService.mode === 'dark';
    this.themeService.mode$.pipe(takeUntil(this.destroy$)).subscribe((m) => {
      this.darkMode = m === 'dark';
    });
    this.translationService.currentLanguage$.pipe(takeUntil(this.destroy$)).subscribe(() => this.cdr.markForCheck());
  }

  toggleTheme(): void {
    this.themeService.toggleTheme();
  }

  openCommandPalette(): void {
    this.commandPalette.open();
  }

  toggleProfileMenu(): void {
    this.profileMenuOpen = !this.profileMenuOpen;
  }

  closeProfileMenu(): void {
    this.profileMenuOpen = false;
  }

  logout(): void {
    this.profileMenuOpen = false;
    this.authService.logout();
    this.router.navigate(['/auth/signin']);
  }

  public resolveDisplayName(user: AuthUser | null): string {
    if (!user) return '';
    const lang = localStorage.getItem('erp_language') === 'en' ? 'en' : 'ar';
    const name = resolveProfileFullName(user.profile, lang);
    return name || user.username || user.email || '';
  }

  public resolveRoleKey(user: AuthUser | null): string {
    if (!user) return 'PROFILE.TITLE';
    const role = (user.role || '').toUpperCase();
    return role === 'ADMIN' ? 'AUTH.ROLE_ADMIN' : role === 'ACCOUNTANT' ? 'AUTH.ROLE_ACCOUNTANT' : 'PROFILE.TITLE';
  }

  public resolveAvatarUrl(user: AuthUser | null): string {
    const image = (user?.profile?.profileImage || '').trim();
    return image || 'assets/images/user/avatar-1.jpg';
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
