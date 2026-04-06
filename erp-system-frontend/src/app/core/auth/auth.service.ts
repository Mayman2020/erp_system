import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { finalize, map, tap } from 'rxjs/operators';
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
  private readonly menuCacheKey = 'erp_ui_menu_cache';
  private readonly authenticatedSubject = new BehaviorSubject<boolean>(!!localStorage.getItem(this.tokenKey));
  private readonly currentUserSubject = new BehaviorSubject<AuthUser | null>(null);
  private readonly loadingUserSubject = new BehaviorSubject<boolean>(false);

  constructor(private http: HttpClient, private permissionService: PermissionService) {}

  get isAuthenticated$(): Observable<boolean> {
    return this.authenticatedSubject.asObservable();
  }

  get currentUser$(): Observable<AuthUser | null> {
    return this.currentUserSubject.asObservable();
  }

  get loadingUser$(): Observable<boolean> {
    return this.loadingUserSubject.asObservable();
  }

  get currentUser(): AuthUser | null {
    return this.currentUserSubject.value;
  }

  get token(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  login(payload: LoginRequest): Observable<LoginResponse> {
    const requestPayload = {
      usernameOrEmail: (payload.usernameOrEmail ?? payload.email ?? '').trim(),
      password: payload.password
    };

    return this.http.post<ApiResponse<LoginResponse>>(`${environment.apiBaseUrl}/auth/login`, requestPayload).pipe(
      map((res) => res.data),
      tap((response) => {
        localStorage.setItem(this.tokenKey, response.token);
        localStorage.removeItem(this.menuCacheKey);
        this.authenticatedSubject.next(true);
        this.permissionService.refresh().subscribe({ error: () => undefined });
        this.refreshCurrentUser();
      })
    );
  }

  resolveLoginRoles(usernameOrEmail: string): Observable<LoginUserType[]> {
    return this.http
      .post<ApiResponse<LoginUserType[]>>(`${environment.apiBaseUrl}/auth/login/roles`, { usernameOrEmail })
      .pipe(map((res) => res.data || []));
  }

  sendPasswordResetOtp(email: string): Observable<boolean> {
    return this.http
      .post<ApiResponse<boolean>>(`${environment.apiBaseUrl}/auth/password/otp/send`, { email })
      .pipe(map((res) => !!res.data));
  }

  resetPasswordWithOtp(email: string, otpCode: string, newPassword: string): Observable<boolean> {
    return this.http
      .post<ApiResponse<boolean>>(`${environment.apiBaseUrl}/auth/password/otp/reset`, { email, otpCode, newPassword })
      .pipe(map((res) => !!res.data));
  }

  logout(): void {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.menuCacheKey);
    this.authenticatedSubject.next(false);
    this.currentUserSubject.next(null);
    this.permissionService.refresh().subscribe({ error: () => undefined });
  }

  refreshCurrentUser(): void {
    if (!this.token) {
      this.currentUserSubject.next(null);
      return;
    }
    this.getMyProfile().subscribe({ error: () => {} });
  }

  getMyProfile(): Observable<AuthUser> {
    this.loadingUserSubject.next(true);
    return this.http.get<ApiResponse<AuthUser>>(`${environment.apiBaseUrl}/profile/me`).pipe(
      map((res) => res.data),
      tap((user) => this.currentUserSubject.next(user)),
      finalize(() => this.loadingUserSubject.next(false))
    );
  }

  updateMyProfile(payload: UpdateProfileRequest): Observable<AuthUser> {
    return this.http.put<ApiResponse<AuthUser>>(`${environment.apiBaseUrl}/profile/me`, payload).pipe(
      map((res) => res.data),
      tap((user) => this.currentUserSubject.next(user))
    );
  }
}
