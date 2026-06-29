import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot } from '@angular/router';
import { Observable, of } from 'rxjs';
import { map, switchMap, take } from 'rxjs/operators';
import { AuthService } from './auth.service';
import { PermissionService } from '../services/permission.service';
import { UiPermission } from '../models/admin.models';

@Injectable({ providedIn: 'root' })
export class PermissionGuard implements CanActivate {
  constructor(
    private authService: AuthService,
    private permissionService: PermissionService,
    private router: Router
  ) {}

  canActivate(route: ActivatedRouteSnapshot, _state: RouterStateSnapshot): Observable<boolean> {
    const menuItemId = route.data['menuItemId'] as string | undefined;
    const action = (route.data['permissionAction'] as keyof UiPermission | undefined) || 'canView';

    if (!menuItemId) {
      return of(true);
    }

    return this.authService.currentUser$.pipe(
      take(1),
      switchMap((user) => {
        if (!user) {
          this.router.navigate(['/auth/signin']);
          return of(false);
        }
        const roles = user.roles || (user.role ? [user.role] : []);
        if (roles.includes('ADMIN')) {
          return of(true);
        }
        return this.permissionService.can(menuItemId, action).pipe(
          map((allowed) => {
            if (!allowed) {
              this.router.navigate(['/dashboard']);
              return false;
            }
            return true;
          })
        );
      })
    );
  }
}
