import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { catchError, map, shareReplay, tap } from 'rxjs/operators';
import { UiPermission } from '../models/admin.models';
import { AdminApiService } from './admin-api.service';

@Injectable({ providedIn: 'root' })
export class PermissionService {
  private permissions$?: Observable<Map<string, UiPermission>>;

  constructor(private adminApi: AdminApiService) {}

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
    return this.getPermissions().pipe(map((permissions) => !!permissions.get(menuItemId)?.[action]));
  }
}
