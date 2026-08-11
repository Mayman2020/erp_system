import { HttpClient } from '@angular/common/http';
import { Injectable, Injector } from '@angular/core';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { catchError, finalize, map, tap } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/api.models';
import { PermissionService } from '../services/permission.service';

export interface LoginRequest {
  email?: string;
  usernameOrEmail?: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  refreshToken?: string;
}

export type LoginUserType = 'ADMIN' | 'ACCOUNTANT';

export interface UserProfile {
  id?: number;
  userId?: number;
  /** Resolved for Accept-Language at response time; use `resolveProfileFullName` when UI language changes without refetch. */
  fullName?: string;
  fullNameEn?: string;
  fullNameAr?: string;
  profileImage?: string;
  nationalId?: string;
  companyName?: string;
  companyNameEn?: string | null;
  companyNameAr?: string | null;
}

/** Display name from bilingual profile fields using current UI language (localStorage `erp_language`). */
export function resolveProfileFullName(profile: UserProfile | null | undefined, lang: string): string {
  if (!profile) {
    return '';
  }
  const preferAr = lang === 'ar';
  const en = (profile.fullNameEn || '').trim();
  const ar = (profile.fullNameAr || '').trim();
  const legacy = (profile.fullName || '').trim();
  if (preferAr) {
    return ar || legacy || en;
  }
  return en || legacy || ar;
}

export function resolveProfileCompanyName(profile: UserProfile | null | undefined, lang: string): string {
  if (!profile) {
    return '';
  }
  const preferAr = lang === 'ar';
  const en = (profile.companyNameEn || '').trim();
  const ar = (profile.companyNameAr || '').trim();
  const legacy = (profile.companyName || '').trim();
  if (preferAr) {
    return ar || legacy || en;
  }
  return en || legacy || ar;
}

export interface AuthUser {
  id: number;
  username: string;
  email: string;
  phone: string;
  role?: string;
  roles?: string[];
  active?: boolean;
  mustChangePassword?: boolean;
  createdAt?: string;
  profile?: UserProfile | null;
}

export interface UpdateProfileRequest {
  username: string;
  email: string;
  phone: string;
  fullNameEn: string;
  fullNameAr: string;
  profileImage?: string | null;
  nationalId?: string | null;
  companyNameEn?: string | null;
  companyNameAr?: string | null;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly tokenKey = 'erp_auth_token';
  private readonly refreshTokenKey = 'erp_auth_refresh_token';
  private readonly menuCacheKey = 'erp_ui_menu_cache_v2';
  private readonly activeRoleKey = 'erp_active_role';
  private readonly authenticatedSubject = new BehaviorSubject<boolean>(!!localStorage.getItem(this.tokenKey));
  private readonly currentUserSubject = new BehaviorSubject<AuthUser | null>(null);
  private readonly loadingUserSubject = new BehaviorSubject<boolean>(false);
  private readonly activeRoleSubject = new BehaviorSubject<string | null>(localStorage.getItem(this.activeRoleKey));

  constructor(private http: HttpClient, private injector: Injector) {}

  private refreshPermissions(): void {
    this.injector.get(PermissionService).refresh().subscribe({ error: () => undefined });
  }

  get isAuthenticated$(): Observable<boolean> {
    return this.authenticatedSubject.asObservable();
  }

  get currentUser$(): Observable<AuthUser | null> {
    return this.currentUserSubject.asObservable();
  }

  get loadingUser$(): Observable<boolean> {
    return this.loadingUserSubject.asObservable();
  }

  get activeRoleChanged(): Observable<string | null> {
    return this.activeRoleSubject.asObservable();
  }

  get currentUser(): AuthUser | null {
    return this.currentUserSubject.value;
  }

  get token(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  get activeRole(): string | null {
    return this.activeRoleSubject.value;
  }

  getEffectiveRoles(): string[] {
    const user = this.currentUser;
    const roles = user?.roles?.length ? user.roles : (user?.role ? [user.role] : []);
    return [...new Set(roles.map((role) => role.trim().toUpperCase()).filter(Boolean))];
  }

  setActiveRole(roleCode: string): boolean {
    const normalized = (roleCode || '').trim().toUpperCase();
    if (!normalized || !this.getEffectiveRoles().includes(normalized)) {
      return false;
    }
    if (this.activeRoleSubject.value === normalized) {
      return true;
    }
    localStorage.setItem(this.activeRoleKey, normalized);
    this.activeRoleSubject.next(normalized);
    return true;
  }

  login(payload: LoginRequest): Observable<LoginResponse> {
    const requestPayload = {
      usernameOrEmail: (payload.usernameOrEmail ?? payload.email ?? '').trim(),
      password: payload.password
    };

    return this.http.post<ApiResponse<LoginResponse>>(`${environment.apiUrl}/auth/login`, requestPayload).pipe(
      map((res) => res.data),
      tap((response) => {
        localStorage.setItem(this.tokenKey, response.token);
        if (response.refreshToken) {
          localStorage.setItem(this.refreshTokenKey, response.refreshToken);
        }
        localStorage.removeItem(this.menuCacheKey);
        localStorage.removeItem('erp_ui_menu_cache');
        localStorage.removeItem(this.activeRoleKey);
        this.activeRoleSubject.next(null);
        this.authenticatedSubject.next(true);
        this.refreshPermissions();
        this.refreshCurrentUser();
      })
    );
  }

  resolveLoginRoles(usernameOrEmail: string): Observable<LoginUserType[]> {
    return this.http
      .post<ApiResponse<LoginUserType[]>>(`${environment.apiUrl}/auth/login/roles`, { usernameOrEmail })
      .pipe(map((res) => res.data || []));
  }

  sendPasswordResetOtp(email: string): Observable<boolean> {
    return this.http
      .post<ApiResponse<boolean>>(`${environment.apiUrl}/auth/password/otp/send`, { email })
      .pipe(map((res) => !!res.data));
  }

  resetPasswordWithOtp(email: string, otpCode: string, newPassword: string): Observable<boolean> {
    return this.http
      .post<ApiResponse<boolean>>(`${environment.apiUrl}/auth/password/otp/reset`, { email, otpCode, newPassword })
      .pipe(map((res) => !!res.data));
  }

  logout(): void {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.refreshTokenKey);
    localStorage.removeItem(this.menuCacheKey);
    localStorage.removeItem('erp_ui_menu_cache');
    localStorage.removeItem(this.activeRoleKey);
    this.authenticatedSubject.next(false);
    this.currentUserSubject.next(null);
    this.activeRoleSubject.next(null);
    this.refreshPermissions();
  }

  refreshCurrentUser(): void {
    if (!this.token) {
      this.currentUserSubject.next(null);
      return;
    }
    this.getMyProfile().subscribe({ error: () => {} });
  }

  /** Resolves once with the current user (or null), used by APP_INITIALIZER so route guards never
   * evaluate before the profile fetch completes (avoids denying access on a hard refresh). */
  initCurrentUser(): Observable<AuthUser | null> {
    if (!this.token) {
      this.currentUserSubject.next(null);
      return of(null);
    }
    return this.getMyProfile().pipe(
      catchError(() => {
        this.currentUserSubject.next(null);
        return of(null);
      })
    );
  }

  getMyProfile(): Observable<AuthUser> {
    this.loadingUserSubject.next(true);
    return this.http.get<ApiResponse<AuthUser>>(`${environment.apiUrl}/profile/me`).pipe(
      map((res) => res.data),
      tap((user) => {
        this.currentUserSubject.next(user);
        this.syncActiveRole(user);
      }),
      finalize(() => this.loadingUserSubject.next(false))
    );
  }

  updateMyProfile(payload: UpdateProfileRequest): Observable<AuthUser> {
    return this.http.put<ApiResponse<AuthUser>>(`${environment.apiUrl}/profile/me`, payload).pipe(
      map((res) => res.data),
      tap((user) => this.currentUserSubject.next(user))
    );
  }

  changePassword(currentPassword: string, newPassword: string): Observable<AuthUser> {
    return this.http.put<ApiResponse<AuthUser>>(`${environment.apiUrl}/profile/me/password`, { currentPassword, newPassword }).pipe(
      map((res) => res.data),
      tap((user) => this.currentUserSubject.next(user))
    );
  }

  private syncActiveRole(user: AuthUser): void {
    const roles = [...new Set((user.roles?.length ? user.roles : (user.role ? [user.role] : []))
      .map((role) => role.trim().toUpperCase())
      .filter(Boolean))];
    const persisted = localStorage.getItem(this.activeRoleKey)?.trim().toUpperCase() || null;
    const nextRole = persisted && roles.includes(persisted)
      ? persisted
      : ((user.role || '').trim().toUpperCase() || roles[0] || null);
    if (nextRole) {
      localStorage.setItem(this.activeRoleKey, nextRole);
    } else {
      localStorage.removeItem(this.activeRoleKey);
    }
    if (this.activeRoleSubject.value !== nextRole) {
      this.activeRoleSubject.next(nextRole);
    }
  }
}
