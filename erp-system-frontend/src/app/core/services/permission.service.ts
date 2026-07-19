import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { catchError, map, shareReplay } from 'rxjs/operators';
import { UiPermission } from '../models/admin.models';
import { AuthService } from '../auth/auth.service';
import { AdminApiService } from './admin-api.service';

@Injectable({ providedIn: 'root' })
export class PermissionService {
  private permissions$?: Observable<Map<string, UiPermission>>;

  constructor(private adminApi: AdminApiService, private authService: AuthService) {}

  /** Eager load at app init when a session token exists. */
  loadMine(): Observable<Map<string, UiPermission> | null> {
    if (!this.authService.token) {
      return of(null);
    }
    return this.getPermissions().pipe(catchError(() => of(new Map<string, UiPermission>())));
  }

  getPermissions(): Observable<Map<string, UiPermission>> {
    if (!this.permissions$) {
      this.permissions$ = this.adminApi.getUiPermissions().pipe(
        map((items) => new Map(items.map((item) => [item.menuItemId, item]))),
        catchError(() => of(new Map<string, UiPermission>())),
        shareReplay(1)
      );
    }
    return this.permissions$;
  }

  refresh(): Observable<Map<string, UiPermission>> {
    this.permissions$ = undefined;
    return this.getPermissions();
  }

  can(menuItemId: string, action: keyof UiPermission): Observable<boolean> {
    const user = this.authService.currentUser;
    const roles = user?.roles || (user?.role ? [user.role] : []);
    if (roles.includes('ADMIN')) {
      return of(true);
    }
    return this.getPermissions().pipe(map((permissions) => !!permissions.get(menuItemId)?.[action]));
  }
}
