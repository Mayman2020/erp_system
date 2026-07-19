import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { NextConfig } from '../../../../app-config';
import { AuthService, AuthUser, resolveProfileFullName } from '../../../../core/auth/auth.service';
import { TranslationService } from '../../../../core/i18n/translation.service';
import { Router } from '@angular/router';

@Component({
  standalone: false,
  selector: 'app-navigation',
  templateUrl: './navigation.component.html',
  styleUrls: ['./navigation.component.scss']
})
export class NavigationComponent implements OnInit, OnDestroy {
  public windowWidth: number;
  public flatConfig: any;
  /** Synced from admin: true = icon-only sidebar */
  @Input() sidebarCollapsed = false;
  @Output() onNavCollapse = new EventEmitter();
  @Output() onNavMobCollapse = new EventEmitter();

  displayName = '';
  roleLabel = '';
  avatarUrl = '';
  userInitials = 'U';

  private currentUser: AuthUser | null = null;
  private readonly destroy$ = new Subject<void>();

  constructor(
    private readonly authService: AuthService,
    private readonly translationService: TranslationService,
    private readonly router: Router
  ) {
    this.flatConfig = NextConfig.config;
    this.windowWidth = window.innerWidth;
  }

  ngOnInit(): void {
    this.authService.currentUser$.pipe(takeUntil(this.destroy$)).subscribe((user) => {
      this.currentUser = user;
      this.refreshUserFooter();
    });
    this.translationService.currentLanguage$.pipe(takeUntil(this.destroy$)).subscribe(() => this.refreshUserFooter());
    this.authService.refreshCurrentUser();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  navCollapse() {
    if (this.windowWidth >= 992) {
      this.onNavCollapse.emit();
    } else {
      this.onNavMobCollapse.emit();
    }
  }

  navMobCollapse() {
    if (this.windowWidth < 992) {
      this.onNavMobCollapse.emit();
    }
  }

  logout(): void { this.authService.logout(); void this.router.navigate(['/auth/signin']); }

  private refreshUserFooter(): void {
    const user = this.currentUser;
    const lang = this.translationService.currentLanguage === 'en' ? 'en' : 'ar';
    const name = resolveProfileFullName(user?.profile, lang) || user?.username || user?.email || '';
    this.displayName = name;
    this.userInitials = (name || 'U').slice(0, 2).toUpperCase();
    const role = (user?.role || '').toUpperCase();
    this.roleLabel = role === 'ADMIN' ? 'AUTH.ROLE_ADMIN' : role === 'ACCOUNTANT' ? 'AUTH.ROLE_ACCOUNTANT' : '';
    const image = (user?.profile?.profileImage || '').trim();
    this.avatarUrl = image || '';
  }
}
