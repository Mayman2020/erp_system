import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { map, tap } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/api.models';

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
  fullName?: string;
  profileImage?: string;
  nationalId?: string;
  companyName?: string;
}

export interface AuthUser {
  id: number;
  username: string;
  email: string;
  phone: string;
  role?: string;
  active?: boolean;
  createdAt?: string;
  profile?: UserProfile | null;
}

export interface UpdateProfileRequest {
  username: string;
  email: string;
  phone: string;
  fullName: string;
  profileImage?: string | null;
  nationalId?: string | null;
  companyName?: string | null;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly tokenKey = 'erp_auth_token';
  private readonly authenticatedSubject = new BehaviorSubject<boolean>(!!localStorage.getItem(this.tokenKey));
  private readonly currentUserSubject = new BehaviorSubject<AuthUser | null>(null);

  constructor(private http: HttpClient) {}

  get isAuthenticated$(): Observable<boolean> {
    return this.authenticatedSubject.asObservable();
  }

  get currentUser$(): Observable<AuthUser | null> {
    return this.currentUserSubject.asObservable();
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
        this.authenticatedSubject.next(true);
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
    this.authenticatedSubject.next(false);
    this.currentUserSubject.next(null);
  }

  refreshCurrentUser(): void {
    if (!this.token) {
      this.currentUserSubject.next(null);
      return;
    }
    this.getMyProfile().subscribe({ error: () => {} });
  }

  getMyProfile(): Observable<AuthUser> {
    return this.http.get<ApiResponse<AuthUser>>(`${environment.apiBaseUrl}/profile/me`).pipe(
      map((res) => res.data),
      tap((user) => this.currentUserSubject.next(user))
    );
  }

  updateMyProfile(payload: UpdateProfileRequest): Observable<AuthUser> {
    return this.http.put<ApiResponse<AuthUser>>(`${environment.apiBaseUrl}/profile/me`, payload).pipe(
      map((res) => res.data),
      tap((user) => this.currentUserSubject.next(user))
    );
  }
}
