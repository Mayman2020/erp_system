import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivateChild, Router } from '@angular/router';
import { Observable, of } from 'rxjs';
import { map, take } from 'rxjs/operators';
import { AuthService } from './auth.service';

const ALLOWED_PATH = 'force-password-change';

@Injectable({ providedIn: 'root' })
export class MustChangePasswordGuard implements CanActivateChild {
  constructor(private authService: AuthService, private router: Router) {}

  canActivateChild(route: ActivatedRouteSnapshot): Observable<boolean> {
    if (route.routeConfig?.path === ALLOWED_PATH) {
      return of(true);
    }
    return this.authService.currentUser$.pipe(
      take(1),
      map((user) => {
        if (!user?.mustChangePassword) {
          return true;
        }
        this.router.navigate(['/' + ALLOWED_PATH]);
        return false;
      })
    );
  }
}
